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
package debrepo.teamcity.entity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface DebRepositoryConfiguration extends Comparable<DebRepositoryConfiguration> {
	
	public abstract String getProjectId();
	public abstract void setProjectId(String projectId);
	public abstract UUID getUuid();
	public abstract String getRepoName();
	public abstract void setRepoName(String repoName);
	public abstract boolean isRestricted();
	public abstract void setRestricted(boolean restricted);
	public abstract boolean containsBuildType(String buildTypeid);
	public abstract List<DebRepositoryBuildTypeConfig> getBuildTypes();
	public abstract boolean addBuildType(DebRepositoryBuildTypeConfig buildTypeConfig);
	public abstract Set<String> getArchitecturesRepresentedByAll();
	public abstract void setArchitecturesRepresentedByAll(Set<String> archs);
	public abstract Set<String> getDefaultArchitecturesRepresentedByAll();

}
