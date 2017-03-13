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

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;

import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.rauschig.jarchivelib.ArchiveEntry;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.ArchiveStream;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;
import org.rauschig.jarchivelib.IOUtils;

public class ArStreamerTest {
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	@Test
	public void getThing(){
		ArArchiveEntry e = new ArArchiveEntry(
									new File("src/test/resources/build-essential_11.6ubuntu6_amd64.deb"),
									"control.tar.gz");
		System.out.println(e.getLength());
	}
	
	@Test
	public void getStream() throws IOException {
		
		Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.AR);
		ArchiveStream stream = archiver.stream(new File("src/test/resources/build-essential_11.6ubuntu6_amd64.deb"));
		org.rauschig.jarchivelib.ArchiveEntry entry;
		

		while((entry = stream.getNextEntry()) != null) {
		    // access each archive entry individually using the stream
		    // or extract it using entry.extract(destination)
		    // or fetch meta-data using entry.getName(), entry.isDirectory(), ...
			System.out.println(entry.getName());
			if (entry.getName().equals("control.tar.gz")){
				//ArchiveStream controlTarGzStream = archivertgz.stream(entry.);
				
			}
		}
		stream.close();
	}
	
	@Test
	public void getControlFileAsStringTest() throws IOException {
		Path ephemeralTempDir = Files.createTempDirectory(Paths.get("target"), "deb-temp-", new FileAttribute<?>[] {});
		File debFile = new File("src/test/resources/build-essential_11.6ubuntu6_amd64.deb");
		DebFileReaderImpl reader = new DebFileReaderImpl(new File("src/test/resources"), "target");
		File controlTarGz = reader.getControlTarGzFromDeb(debFile, ephemeralTempDir.toFile());
		String controlFileContents = reader.getControlFromControlTarGz(controlTarGz);
		System.out.println(controlFileContents);
		Map<String,String> params = reader.getDebItemsFromControl(debFile, controlFileContents);
		
		assertTrue(params.size() > 0);
		for (String key: params.keySet()){
			System.out.println("##" + key + "##:??" + params.get(key) + "??");
		}
	}
	
	@Test
	public void getControlFileAsStringFromAllTest() throws IOException {
		Path ephemeralTempDir = Files.createTempDirectory(Paths.get("target"), "deb-temp-", new FileAttribute<?>[] {});
		File debFile = new File("src/test/resources/packages_for_testing/debhelper_9.20120909_all.deb");
		DebFileReaderImpl reader = new DebFileReaderImpl(new File("src/test/resources/packages_for_testing"), "target");
		File controlTarGz = reader.getControlTarGzFromDeb(debFile, ephemeralTempDir.toFile());
		String controlFileContents = reader.getControlFromControlTarGz(controlTarGz);
		System.out.println(controlFileContents);
		Map<String,String> params = reader.getDebItemsFromControl(debFile, controlFileContents);
		
		assertTrue(params.size() > 0);
		for (String key: params.keySet()){
			System.out.println("##" + key + "##:??" + params.get(key) + "??");
		}
	}
	
	@Test
	public void getControlFileAsStringFromAllTest2() throws IOException {
		Path ephemeralTempDir = Files.createTempDirectory(Paths.get("target"), "deb-temp-", new FileAttribute<?>[] {});
		File debFile = new File("src/test/resources/packages_for_testing/autoconf_2.69-8_all.deb");
		DebFileReaderImpl reader = new DebFileReaderImpl(new File("src/test/resources/packages_for_testing"), "target");
		File controlTarGz = reader.getControlTarGzFromDeb(debFile, ephemeralTempDir.toFile());
		String controlFileContents = reader.getControlFromControlTarGz(controlTarGz);
		System.out.println(controlFileContents);
		Map<String,String> params = reader.getDebItemsFromControl(debFile, controlFileContents);
		
		assertTrue(params.size() > 0);
		for (String key: params.keySet()){
			System.out.println("##" + key + "##:??" + params.get(key) + "??");
		}
	}
	
	@Test
	public void getControlFileAsStringTest2() throws IOException {
		Path ephemeralTempDir = Files.createTempDirectory(Paths.get("target"), "deb-temp-", new FileAttribute<?>[] {});
		File debFile = new File("src/test/resources/packages_for_testing/e3_2.71-1_amd64.deb");
		DebFileReaderImpl reader = new DebFileReaderImpl(new File("src/test/resources/packages_for_testing"), "target");
		File controlTarGz = reader.getControlTarGzFromDeb(debFile, ephemeralTempDir.toFile());
		String controlFileContents = reader.getControlFromControlTarGz(controlTarGz);
		System.out.println(controlFileContents);
		Map<String,String> params = reader.getDebItemsFromControl(debFile, controlFileContents);
		
		assertTrue(params.size() > 0);
		for (String key: params.keySet()){
			System.out.println("##" + key + "##:??" + params.get(key) + "??");
		}
	}
	
	
	public String getControlStringFromArFile(File arFile) throws IOException {
		Archiver archiverAr = ArchiverFactory.createArchiver(ArchiveFormat.AR);
		Archiver archivertgz = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);
		ArchiveStream stream = archiverAr.stream(arFile);
		ArchiveEntry entry, entry2;
		ByteArrayOutputStream baos= new ByteArrayOutputStream();
		
		while((entry = stream.getNextEntry()) != null) {
			// The ar contains a tgz file named control.tar.gz
			if (entry.getName().equals("control.tar.gz")) {
				ArchiveStream stream2 = null; //archivertgz.stream(entry);
				while((entry2 = stream2.getNextEntry()) != null) {
					//The control.tar.gz contains a text file named control 
					if (entry2.getName().equals("./control")){
						IOUtils.copy(stream2, baos);
					}
				}
			}
		}
		
		return baos.toString( StandardCharsets.UTF_8.toString() );
	}
	
	

}
