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
package debrepo.teamcity.service;

import java.util.List;
import java.util.Set;

import debrepo.teamcity.entity.DebPackageEntity;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryConfigurations;

public interface DebRepositoryConfigurationManager {

	public void updateRepositoryConfigurations(DebRepositoryConfigurations repoConfigurations);
	public List<DebRepositoryConfiguration> getConfigurationsForProject(String projectId);
	public Set<DebRepositoryConfiguration> findConfigurationsForBuildType(String buildTypeId);
	/**
	 * 
	 * @param debPackageEntity which must be fully populated and variables resolved.
	 * @return {@link Set} of {@link DebRepositoryConfiguration} items which have a matching buildTypeId, 
	 * 			and a filter which matches the regex against the filename, the dist is equal and the component is equal. 
	 */
	public Set<DebRepositoryConfiguration> findConfigurationsForDebRepositoryEntity(DebPackageEntity debPackageEntity);
	
}
