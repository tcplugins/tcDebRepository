/*******************************************************************************
 *
 *  Copyright 2016 Net Wolf UK
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  
 *******************************************************************************/
package debrepo.teamcity.entity.helper;

import java.util.Collection;
import java.util.Map.Entry;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.RepositoryFile;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import jetbrains.buildServer.configs.FeatureBuilderImpl;

/**
 * <p>
 * Responsible for generating a Debian Repository compliant Release
 * file. This file contains checksums of all files in "subdirectories"
 * of the current directory. 
 * </p>
 * 
 * 
 * <p>
 *  An example listing looks like this:</p>
 *  <pre>
 *    Origin: Debian
 *    Label: Debian
 *    Suite: stable
 *    Version: 8.6
 *    Codename: jessie
 *    Date: Sat, 17 Sep 2016 11:38:03 UTC
 *    Architectures: amd64 arm64 armel armhf i386 mips mipsel powerpc ppc64el s390x
 *    Components: main contrib non-free
 *    Description: Debian 8.6 Released 17 September 2016
 *    MD5Sum:
 *     8ddfa258717ed543bcee866f3e5afcf4 14176820 main/binary-all/Packages
 *     21aead000dba488fe0105a1c9c17cc03  3940441 main/binary-all/Packages.gz
 *     ed586749f4cbfc71f260f4da1b474ff4  3005304 main/binary-all/Packages.xz
 *     9c8191e434cbc74efb9b3b878929441d       92 main/binary-all/Release
 *     7e86694bd337e5b1fcd98ab7c3a3052a 33958599 main/binary-amd64/Packages
 *     15c510de18465b61be21364e44f3c11c  9063772 main/binary-amd64/Packages.gz
 *     c3765d6c9452eb0f9acf4600562385d3  6786596 main/binary-amd64/Packages.xz
 *     62864e2f736f5980a2521a4dee0fd746       94 main/binary-amd64/Release
 *    SHA1:
 *     212e453a22000b3e31efdda0f5773c9e19ea1ea4 14176820 main/binary-all/Packages
 *     d2a1db51ab2d2ed0b0a8490102844e82db531f6a  3940441 main/binary-all/Packages.gz
 *     448520da34847f2d511c6a2e8b4c22365b8b86dd  3005304 main/binary-all/Packages.xz
 *     0a756af9ec1b4a045e44b976440ca584c623aac7       92 main/binary-all/Release
 *     2fbb19db561bab3b900603719cfb80de5fd7149f 33958599 main/binary-amd64/Packages
 *     f911d5c3177d5b862f14a65e46ac95c5f25a84b4  9063772 main/binary-amd64/Packages.gz
 *     1a4c71b9dc3718a22864120232fb275d5e1f6add  6786596 main/binary-amd64/Packages.xz
 *     71789aac10d6140e1d77c9cea8a45e4400b4d131       94 main/binary-amd64/Release
 *    SHA256:
 *     dab91196653c3e02250bceb1e5a6497a33de5618b64d8da439bab859027a6f33 14176820 main/binary-all/Packages
 *     04ad198706c3a0b74133b5829cac9a36e03d9e179bffc7c7cdbd9ac8ed7d37a1  3940441 main/binary-all/Packages.gz
 *     43248b67a6a678d23cbdad17680438c3a71c6262dc20ef8eed6057392389115e  3005304 main/binary-all/Packages.xz
 *     2292c5b006e899a8addb88d743e8e6fbaa7904a45ab6a8968a187136ccefbe30       92 main/binary-all/Release
 *     e13ae33a0b4b98da0fa2f9fe5be7b266c8a6c4626c58af47a8bfb332d8e2c647 33958599 main/binary-amd64/Packages
 *     26e8275be588d35313eac65a1a88b17a1052eb323255048b13bdf0653421a9f2  9063772 main/binary-amd64/Packages.gz
 *     8b80b6608a8fc72509b949efe1730077f0e8383b29c6aed5f86d9f9b51a631d8  6786596 main/binary-amd64/Packages.xz
 *     809ed288cdd7cb649a31cefef9e7fe0eda99f3e9c81ce970ad91f2904aaa130f       94 main/binary-amd64/Release
 * </pre>
 */
		
public class DebRepositoryToReleaseDescriptionBuilder {
	
	public static String buildReleaseHeader(DebRepositoryConfiguration configuration) {
		StringBuilder sb = new StringBuilder();
		/*
		 *    Origin: Debian
		 *    Label: Debian
		 *    Suite: stable
		 *    Version: 8.6
		 *    Codename: jessie
		 *    Date: Sat, 17 Sep 2016 11:38:03 UTC
		 *    Architectures: amd64 arm64 armel armhf i386 mips mipsel powerpc ppc64el s390x
		 *    Components: main contrib non-free
		 *    Description: Debian 8.6 Released 17 September 2016 */
		
//		sb.append("Origin: ").append(debPackageEntity.getPackageName()).append("\n");
//		for (Entry<String,String> e : debPackageEntity.getParameters().entrySet()) {
//			if (!e.getKey().equals("Package")) {
//				sb.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
//			}
//		}
		return sb.toString();
	}
	
	public static String buildPackageDescriptionList(DebRepositoryConfiguration configuration, Collection<? extends RepositoryFile> repositoryFiles) {
		StringBuilder sb = new StringBuilder();
		sb.append(buildReleaseHeader(configuration));
		
		sb.append("MD5Sum:\n");
		for (RepositoryFile file : repositoryFiles) {
			sb.append(file.getMD5Sum());
			sb.append(" ");
			file.getSizeInKbAndFilename();
			sb.append("\n");
		}
		
		sb.append("SHA1:\n");
		for (RepositoryFile file : repositoryFiles) {
			sb.append(file.getSHA1());
			sb.append(" ");
			file.getSizeInKbAndFilename();
			sb.append("\n");
		}
		
		sb.append("SHA256:\n");
		for (RepositoryFile file : repositoryFiles) {
			sb.append(file.getSHA256());
			sb.append(" ");
			file.getSizeInKbAndFilename();
			sb.append("\n");
		}
		return sb.toString();
	}
	

}
