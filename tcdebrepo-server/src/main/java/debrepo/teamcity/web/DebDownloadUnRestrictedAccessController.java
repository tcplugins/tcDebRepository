/*******************************************************************************
 *
 *  Copyright 2017 Net Wolf UK
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
package debrepo.teamcity.web;

import debrepo.teamcity.service.DebReleaseFileLocator;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.NonExistantRepositoryException;
import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

public class DebDownloadUnRestrictedAccessController extends DebDownloadController {

	public DebDownloadUnRestrictedAccessController(SBuildServer sBuildServer, WebControllerManager webControllerManager,
			PluginDescriptor descriptor, AuthorizationInterceptor authorizationInterceptor,
			DebRepositoryManager debRepositoryManager, DebRepositoryConfigurationManager debRepositoryConfigurationManager, 
			SecurityContext securityContext, DebReleaseFileLocator debReleaseFileLocator) {
		super(sBuildServer, webControllerManager, descriptor, authorizationInterceptor, debRepositoryManager, debReleaseFileLocator);
	}
	
	@Override
	protected void configureUrlAndAuthorisation(WebControllerManager webControllerManager, AuthorizationInterceptor authorizationInterceptor) {
		webControllerManager.registerController(DEBREPO_BASE_URL_UNRESTRICTED_WITH_WILDCARD, this);
		authorizationInterceptor.addPathNotRequiringAuth(DEBREPO_BASE_URL_UNRESTRICTED_WITH_WILDCARD);
	}

	@Override
	protected void checkRepoIsRestricted(String repoName) throws DebRepositoryAccessIsRestrictedException, DebRepositoryPermissionDeniedException, NonExistantRepositoryException {
		if (myDebRepositoryManager.isRestrictedRepository(repoName)) {
			throw new DebRepositoryAccessIsRestrictedException();
		}
	}
	
	@Override
	protected String getDebRepoUrlPart() {
		return DEBREPO_URL_PART_UNRESTRICTED;
	}

	@Override
	protected String getDebRepoUrlPartWithContext() {

		return DEBREPO_URL_PART_UNRESTRICTED;
	}

}
