/*******************************************************************************
 * Copyright 2016 Net Wolf UK
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package debrepo.teamcity.archive;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.rauschig.jarchivelib.ArchiveEntry;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.ArchiveStream;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;
import org.rauschig.jarchivelib.IOUtils;

import debrepo.teamcity.Loggers;

public class DebFileReader {
	File myArtifactsBaseDirectory;
	String myTempDirectory;
	
	public DebFileReader(File artifactsBaseDirectory, String tempDirectory) {
		this.myArtifactsBaseDirectory = artifactsBaseDirectory;
		this.myTempDirectory = tempDirectory;
	}
	
	public Map<String,String> getMetaDataFromPackage(String filename) throws IOException {
		File debFile = new File(this.myArtifactsBaseDirectory + File.separator + filename);
		File controlTarGz = this.getControlTarGzFromDeb(debFile);
		String controlFileContents = this.getControlFromControlTarGz(controlTarGz);
		return this.getDebItemsFromControl(debFile,controlFileContents);
	}
	
	protected File getControlTarGzFromDeb(File debFile) throws IOException {
		
		Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.AR);
		ArchiveStream stream = archiver.stream(debFile);
		ArchiveEntry entry;
		
		File controlTarGzFile = null;

		while((entry = stream.getNextEntry()) != null) {
			if ("control.tar.gz".equals(entry.getName())){
				controlTarGzFile = entry.extract(new File(this.myTempDirectory));
			}
		}
		stream.close();
		
		return controlTarGzFile;
	}
	
	protected String getControlFromControlTarGz(File controlTarGzFile) throws IOException {
		Archiver archivertgz = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);
		ArchiveStream stream = archivertgz.stream(controlTarGzFile);
		ArchiveEntry entry;
		ByteArrayOutputStream baos= new ByteArrayOutputStream();
		
		while((entry = stream.getNextEntry()) != null) {
			if ("./control".equals(entry.getName())){
				IOUtils.copy(stream, baos);
			}
		}
		
		stream.close();
		
		return baos.toString( StandardCharsets.UTF_8.toString() );
	}
	
	protected Map<String, String> getDebItemsFromControl(File debFile, String controlFileContents) {
		Pattern p = Pattern.compile("^(\\S+):(.+)$");
		
		Map<String, String> map = new LinkedHashMap<String, String>();
		Scanner scanner = new Scanner(controlFileContents);
		
		while (scanner.hasNextLine()) {
		  Matcher m = p.matcher(scanner.nextLine());
		  if (m.find()){
			  map.put(m.group(1), m.group(2).trim());
		  }
		}
		scanner.close();
		
		map.putAll(getExtraPackageItemsFromDeb(debFile));
		
		return map;
	}
	
	protected Map<String,String> getExtraPackageItemsFromDeb(File debFile) {
		/*
		 * Filename: pool/main/b/build-essential/build-essential_11.6ubuntu6_amd64.deb
		 * Size: 4838
		 * MD5sum: 6fa3d082885a7440d512236685cd24fd
		 * SHA1: 488c10084cd20cafec7f8b917e752bad45a4f983
		 * SHA256: 50c00d2da704e131855abda2f823f3ac2589ab1579f511ccd005be421f0a3954 
		 * 
		 */
		Map<String, String> map = new LinkedHashMap<>();
		try {
			map.put("Size",  String.valueOf(debFile.length()));
			map.put("MD5sum", getMd5Hash(debFile));
			map.put("SHA1", getSha1Hash(debFile));
			map.put("SHA256", getSha256Hash(debFile));
		} catch (IOException e){
			Loggers.SERVER.warn("DebFileReader:: Failed to generate file hash. " + e.getMessage());
			if (Loggers.SERVER.isDebugEnabled()) { Loggers.SERVER.debug(e);}
		}
		
		return map;

	}
	
	protected String getMd5Hash(File debFile) throws IOException {
		FileInputStream fis = new FileInputStream(debFile);
		return DigestUtils.md5Hex(fis);
	}
	
	protected String getSha1Hash(File debFile) throws IOException {
		FileInputStream fis = new FileInputStream(debFile);
		return DigestUtils.sha1Hex(fis);
	}
	
	protected String getSha256Hash(File debFile) throws IOException {
		FileInputStream fis = new FileInputStream(debFile);
		return DigestUtils.sha256Hex(fis);
	}
	
}
