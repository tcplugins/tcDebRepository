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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.rauschig.jarchivelib.ArchiveEntry;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.ArchiveStream;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;
import org.rauschig.jarchivelib.IOUtils;

public class DebFileReader {
	File debFile;
	
	public DebFileReader(File deb) {
		this.debFile = deb;
	}
	
	
	
	protected File getControlTarGzFromDeb(File tmpLocation) throws IOException {
		
		Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.AR);
		ArchiveStream stream = archiver.stream(this.debFile);
		ArchiveEntry entry;
		
		File controlTarGzFile = null;

		while((entry = stream.getNextEntry()) != null) {
		    // access each archive entry individually using the stream
		    // or extract it using entry.extract(destination)
		    // or fetch meta-data using entry.getName(), entry.isDirectory(), ...
			System.out.println(entry.getName());
			if (entry.getName().equals("control.tar.gz")){
				controlTarGzFile = entry.extract(tmpLocation);
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
			if (entry.getName().equals("./control")){
				IOUtils.copy(stream, baos);
			}
		}
		
		stream.close();
		
		return baos.toString( StandardCharsets.UTF_8.toString() );
	}
	
	protected Map<String, String> getDebItemsFromControl(String controlFileContents) {
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
		Map<String, String> map = new LinkedHashMap<String, String>();
		try {
			map.put("MD5sum", getFileHashSum("MD5"));
			map.put("SHA1", getFileHashSum("SHA-1"));
			map.put("SHA256", getFileHashSum("SHA-256"));
		} catch (IOException e){
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return map;

	}
	
	protected String getFileHashSum(String digestAlgorithm) throws NoSuchAlgorithmException, IOException{
        MessageDigest md = MessageDigest.getInstance(digestAlgorithm);
        FileInputStream fis = new FileInputStream(debFile);

        byte[] dataBytes = new byte[1024];

        int nread = 0;
        while ((nread = fis.read(dataBytes)) != -1) {
          md.update(dataBytes, 0, nread);
        };
        byte[] mdbytes = md.digest();

       //convert the byte to hex format method 2
        StringBuffer hexString = new StringBuffer();
    	for (int i=0;i<mdbytes.length;i++) {
    	  hexString.append(Integer.toHexString(0xFF & mdbytes[i]));
    	}
    	
    	fis.close();
    	
    	return hexString.toString();
	}

}
