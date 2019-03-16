/*******************************************************************************
 * Copyright 2017 Net Wolf UK
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
package debrepo.teamcity.entity.helper;

import debrepo.teamcity.GenericRepositoryFile;
import lombok.Builder;
import lombok.Value;

@Value @Builder
public class ReleaseFileImpl implements GenericRepositoryFile {
	
	private static final int padnum = 16;
	
	private int sizeInBytes;
	private String path;
	private String md5;
	private String sha1;
	private String sha256;
	private String component;
	private String arch;

	private static void padSize(StringBuilder stringBuilder, String fileSize) {
		int rest = padnum - fileSize.length();
		for(int i = 1; i < rest; i++)
		    {
			stringBuilder.append(" ");
		    }
		stringBuilder.append(fileSize);
	}


	@Override
	public String getFilePath() {
		return path;
	}
	
}
