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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;

import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.ArchiveStream;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

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
	public void getControlFileAsStringTest() throws IOException, DebPackageReadException {
		Path ephemeralTempDir = Files.createTempDirectory(Paths.get("target"), "deb-temp-", new FileAttribute<?>[] {});
		File debFile = new File("src/test/resources/build-essential_11.6ubuntu6_amd64.deb");
		DebFileReaderImpl reader = new DebFileReaderImpl(new File("src/test/resources"), "target");
		DebFileControlFile controlTarGz = reader.getCompressedControlFileFromDeb(debFile, ephemeralTempDir.toFile());
		String controlFileContents = reader.getControlFromCompressedControlFile(controlTarGz);
		System.out.println(controlFileContents);
		Map<String,String> params = reader.getDebItemsFromControl(debFile, controlFileContents);
		
		assertTrue(params.size() > 0);
		for (String key: params.keySet()){
			System.out.println("##" + key + "##:??" + params.get(key) + "??");
		}
	}
	
	@Test
	public void getControlFileAsStringFromAllTest() throws IOException, DebPackageReadException {
		Path ephemeralTempDir = Files.createTempDirectory(Paths.get("target"), "deb-temp-", new FileAttribute<?>[] {});
		File debFile = new File("src/test/resources/packages_for_testing/debhelper_9.20120909_all.deb");
		DebFileReaderImpl reader = new DebFileReaderImpl(new File("src/test/resources/packages_for_testing"), "target");
		DebFileControlFile controlTarGz = reader.getCompressedControlFileFromDeb(debFile, ephemeralTempDir.toFile());
		String controlFileContents = reader.getControlFromCompressedControlFile(controlTarGz);
		System.out.println(controlFileContents);
		Map<String,String> params = reader.getDebItemsFromControl(debFile, controlFileContents);
		
		assertTrue(params.size() > 0);
		for (String key: params.keySet()){
			System.out.println("##" + key + "##:??" + params.get(key) + "??");
		}
	}
	
	@Test
	public void getControlFileAsStringFromAllTest2() throws IOException, DebPackageReadException {
		Path ephemeralTempDir = Files.createTempDirectory(Paths.get("target"), "deb-temp-", new FileAttribute<?>[] {});
		File debFile = new File("src/test/resources/packages_for_testing/autoconf_2.69-8_all.deb");
		DebFileReaderImpl reader = new DebFileReaderImpl(new File("src/test/resources/packages_for_testing"), "target");
		DebFileControlFile controlTarGz = reader.getCompressedControlFileFromDeb(debFile, ephemeralTempDir.toFile());
		String controlFileContents = reader.getControlFromCompressedControlFile(controlTarGz);
		System.out.println(controlFileContents);
		Map<String,String> params = reader.getDebItemsFromControl(debFile, controlFileContents);
		
		assertTrue(params.size() > 0);
		for (String key: params.keySet()){
			System.out.println("##" + key + "##:??" + params.get(key) + "??");
		}
	}
	
	@Test
	public void getControlFileAsStringTest2() throws IOException, DebPackageReadException {
		Path ephemeralTempDir = Files.createTempDirectory(Paths.get("target"), "deb-temp-", new FileAttribute<?>[] {});
		File debFile = new File("src/test/resources/packages_for_testing/e3_2.71-1_amd64.deb");
		DebFileReaderImpl reader = new DebFileReaderImpl(new File("src/test/resources/packages_for_testing"), "target");
		DebFileControlFile controlTarGz = reader.getCompressedControlFileFromDeb(debFile, ephemeralTempDir.toFile());
		String controlFileContents = reader.getControlFromCompressedControlFile(controlTarGz);
		System.out.println(controlFileContents);
		Map<String,String> params = reader.getDebItemsFromControl(debFile, controlFileContents);
		
		assertTrue(params.size() > 0);
		for (String key: params.keySet()){
			System.out.println("##" + key + "##:??" + params.get(key) + "??");
		}
	}
	
	@Test
	public void getControlFileAsStringTest_tcDummyDeb_amd64_1_0_5667() throws IOException, DebPackageReadException {
		Path ephemeralTempDir = Files.createTempDirectory(Paths.get("target"), "deb-temp-", new FileAttribute<?>[] {});
		File debFile = new File("src/test/resources/packages_for_testing/tcDummyDeb_amd64_1.0.5667.deb");
		DebFileReaderImpl reader = new DebFileReaderImpl(new File("src/test/resources/packages_for_testing"), "target");
		DebFileControlFile controlTarGz = reader.getCompressedControlFileFromDeb(debFile, ephemeralTempDir.toFile());
		String controlFileContents = reader.getControlFromCompressedControlFile(controlTarGz);
		System.out.println(controlFileContents);
		Map<String,String> params = reader.getDebItemsFromControl(debFile, controlFileContents);
		
		assertTrue(params.size() > 0);
		for (String key: params.keySet()){
			System.out.println("##" + key + "##:??" + params.get(key) + "??");
		}
	}
	
	@Test
	public void getControlFileAsStringTest_tcDummyDeb_amd64_2_0_14() throws IOException, DebPackageReadException {
		Path ephemeralTempDir = Files.createTempDirectory(Paths.get("target"), "deb-temp-", new FileAttribute<?>[] {});
		File debFile = new File("src/test/resources/packages_for_testing/tcDummyDeb_amd64_2.0.14.deb");
		DebFileReaderImpl reader = new DebFileReaderImpl(new File("src/test/resources/packages_for_testing"), "target");
		DebFileControlFile controlTarGz = reader.getCompressedControlFileFromDeb(debFile, ephemeralTempDir.toFile());
		String controlFileContents = reader.getControlFromCompressedControlFile(controlTarGz);
		System.out.println(controlFileContents);
		Map<String,String> params = reader.getDebItemsFromControl(debFile, controlFileContents);
		
		assertTrue(params.size() > 0);
		for (String key: params.keySet()){
			System.out.println("##" + key + "##:??" + params.get(key) + "??");
		}
	}

}
