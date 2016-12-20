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
package debrepo.teamcity.service;

import java.util.UUID;

import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig.Filter;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryConfigurationJaxImpl;

public class DebRepositoryConfigurationFactoryImpl implements DebRepositoryConfigurationFactory {
	
	@Override
	public DebRepositoryConfigurationJaxImpl createDebRepositoryConfiguration(String projectId, String repositoryName) {
		return new DebRepositoryConfigurationJaxImpl(projectId, repositoryName);
	}

	@Override
	public DebRepositoryConfiguration copyDebRepositoryConfiguration(DebRepositoryConfiguration sourceConfig) {
		DebRepositoryConfigurationJaxImpl newConfig = new DebRepositoryConfigurationJaxImpl(sourceConfig.getProjectId(), sourceConfig.getRepoName());
		newConfig.setUuid(UUID.fromString(sourceConfig.getUuid().toString()));
		for (DebRepositoryBuildTypeConfig btConfig : sourceConfig.getBuildTypes()) {
			DebRepositoryBuildTypeConfig newBtConfig = new DebRepositoryBuildTypeConfig(btConfig.getBuildTypeId());
			for (Filter f : btConfig.getDebFilters()) {
				newBtConfig.addFilter(new Filter(f.getId(), f.getRegex(), f.getDist(), f.getComponent()));
			}
			newConfig.addBuildType(newBtConfig);
		}
		newConfig.getArchitecturesRepresentedByAll().clear();
		for (String arch : sourceConfig.getArchitecturesRepresentedByAll()) {
			newConfig.getArchitecturesRepresentedByAll().add(new String(arch));
		}
		return newConfig;
	}
	
	@Override
	public DebRepositoryConfiguration copyDebRepositoryConfiguration(DebRepositoryConfigurationManager manager, String uuid) {
		return copyDebRepositoryConfiguration(manager.getDebRepositoryConfiguration(uuid));
	}

}
