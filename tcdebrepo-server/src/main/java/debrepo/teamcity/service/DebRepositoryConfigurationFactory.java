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

import debrepo.teamcity.entity.DebRepositoryConfiguration;

public interface DebRepositoryConfigurationFactory {
	/**
	 * Creates a default instance of a DebRepositoryConfiguration
	 * @param uuid
	 * @param repositoryName
	 * @return
	 */
	public abstract DebRepositoryConfiguration createDebRepositoryConfiguration(String projectId, String repositoryName);
	public abstract DebRepositoryConfiguration copyDebRepositoryConfiguration(DebRepositoryConfiguration sourceConfig);
	public abstract DebRepositoryConfiguration copyDebRepositoryConfiguration(DebRepositoryConfigurationManager manager, String uuid);

}
