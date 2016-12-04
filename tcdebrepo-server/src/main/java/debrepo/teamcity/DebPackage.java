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
package debrepo.teamcity;

import java.util.Map;

public interface DebPackage {

	public String getPackageName();

	public void setPackageName(String packageName);

	public String getVersion();

	public void setVersion(String version);

	public String getArch();

	public void setArch(String arch);

	public String getDist();

	public void setDist(String dist);

	public String getComponent();

	public void setComponent(String component);

	public Long getBuildId();

	public void setBuildId(Long sBuildId);

	public String getBuildTypeId();

	public void setBuildTypeId(String sBuildTypeId);

	public String getFilename();

	public void setFilename(String filename);

	public String getUri();

	public void setUri(String uri);

	public Map<String, String> getParameters();

	public void setParameters(Map<String, String> parameters);

	public boolean isPopulated();

	public void populateMetadata(Map<String, String> metaDataFromPackage);

	public void buildUri();

}