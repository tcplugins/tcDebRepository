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
import jetbrains.buildServer.serverSide.auth.AuthUtil;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

public class DebDownloadRestrictedAccessController extends DebDownloadController {

//	public final String DEBREPO_URL_PART = "/debrepo-restricted";
//	public final String DEBREPO_BASE_URL = "/app" + DEBREPO_URL_PART;
//	private final String DEBREPO_BASE_URL_WITH_WILDCARD = DEBREPO_BASE_URL + "/**";
	private final SecurityContext mySecurityContext;
	private DebRepositoryConfigurationManager myDebRepositoryConfigManager;

	public DebDownloadRestrictedAccessController(SBuildServer sBuildServer, WebControllerManager webControllerManager,
			PluginDescriptor descriptor, AuthorizationInterceptor authorizationInterceptor,
			DebRepositoryManager debRepositoryManager, DebRepositoryConfigurationManager debRepositoryConfigurationManager, 
			SecurityContext securityContext, DebReleaseFileLocator debReleaseFileLocator) {
		super(sBuildServer, webControllerManager, descriptor, authorizationInterceptor, debRepositoryManager, debReleaseFileLocator);
		mySecurityContext = securityContext;
		myDebRepositoryConfigManager = debRepositoryConfigurationManager;
	}
	
	@Override
	protected void configureUrlAndAuthorisation(WebControllerManager webControllerManager, AuthorizationInterceptor authorizationInterceptor) {
		webControllerManager.registerController(DEBREPO_BASE_URL_RESTRICTED_WITH_WILDCARD, this);
	}

	@Override
	protected void checkRepoIsRestricted(String repoName) throws DebRepositoryAccessIsRestrictedException, DebRepositoryPermissionDeniedException, NonExistantRepositoryException {
		if (myDebRepositoryManager.isRestrictedRepository(repoName)) {
			String projectId = myDebRepositoryConfigManager.getDebRepositoryConfigurationByName(repoName).getProjectId();
			if (! AuthUtil.hasReadAccessTo(mySecurityContext.getAuthorityHolder(), projectId)) {
				throw new DebRepositoryPermissionDeniedException();
			}
		}
	}
	
	@Override
	protected String getDebRepoUrlPart() {
		return DEBREPO_URL_PART_RESTRICTED;
	}

	@Override
	protected String getDebRepoUrlPartWithContext() {
		return DEBREPO_BASE_URL_RESTRICTED;
	}

}
