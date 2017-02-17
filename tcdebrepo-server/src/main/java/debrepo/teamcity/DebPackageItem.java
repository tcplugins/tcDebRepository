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
package debrepo.teamcity;

import java.util.Map;

import lombok.Data;

@Data
public class DebPackageItem implements DebPackage {
	
	private String packageName;

	private String version;

	private String arch;

	private String dist;

	private String component;

	private Long buildId;
	
	private String buildTypeId;

	private String filename;

	private String uri;
	
	private Map<String, String> parameters;

	@Override
	public boolean isPopulated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void populateMetadata(Map<String, String> metaDataFromPackage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void buildUri() {
		// TODO Auto-generated method stub

	}

}
