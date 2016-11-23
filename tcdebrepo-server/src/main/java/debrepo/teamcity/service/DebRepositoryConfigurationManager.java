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
import debrepo.teamcity.entity.DebRepositoryConfigurationJaxImpl;
import debrepo.teamcity.entity.DebRepositoryConfigurations;
import debrepo.teamcity.service.DebRepositoryConfigurationManager.DebRepositoryActionResult;
import lombok.Value;

public interface DebRepositoryConfigurationManager {


	public void updateRepositoryConfigurations(DebRepositoryConfigurations repoConfigurations);
	public DebRepositoryConfigurationJaxImpl getDebRepositoryConfiguration(String debRepoUuid);
	public List<DebRepositoryConfigurationJaxImpl> getConfigurationsForProject(String projectId);
	public Set<DebRepositoryConfigurationJaxImpl> findConfigurationsForBuildType(String buildTypeId);
	/**
	 * 
	 * @param debPackageEntity which must be fully populated and variables resolved.
	 * @return {@link Set} of {@link DebRepositoryConfigurationJaxImpl} items which have a matching buildTypeId, 
	 * 			and a filter which matches the regex against the filename, the dist is equal and the component is equal. 
	 */
	public Set<DebRepositoryConfigurationJaxImpl> findConfigurationsForDebRepositoryEntity(DebPackageEntity debPackageEntity);
	public DebRepositoryActionResult addDebRepository(DebRepositoryConfigurationJaxImpl debRepositoryConfiguration);
	public DebRepositoryActionResult removeDebRespository(String uuid);
	
	@Value
	public class DebRepositoryActionResult {
		String reason;
		boolean error;
		DebRepositoryConfigurationJaxImpl beforeAction;
		DebRepositoryConfigurationJaxImpl afterAction;

	}

	public DebRepositoryActionResult editDebRepositoryConfiguration(DebRepositoryConfigurationJaxImpl debRepoConfig);


}
