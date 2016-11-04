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
package debrepo.teamcity.web;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import debrepo.teamcity.entity.DebPackageEntity;
import debrepo.teamcity.entity.DebPackageNotFoundInStoreException;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.NonExistantRepositoryException;
import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.WebControllerManager;

public class DebDownloadController extends BaseController {
	
	public static final String DEBREPO_URL_PART = "/debrepo";
	public static final String DEBREPO_BASE_URL = "/app" + DEBREPO_URL_PART;
	private static final String DEBREPO_BASE_URL_WITH_WILDCARD = DEBREPO_BASE_URL + "/**";
	
	/*                                                        /debrepo/{RepoName}/packages.gz		        */
	final private Pattern packagesGzPattern = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+)/[Pp]ackages.gz");
	
	/*                                                        /debrepo/{RepoName}/packages		            */
	final private Pattern packagesPattern   = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+)/[Pp]ackages");
	
	/*                                                        /debrepo/{RepoName}/{Arch}/{PackageName}		*/
    final private Pattern packagePattern    = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+)/(\\S+)/(\\S+)");
    
    private final DebRepositoryManager myDebRepositoryManager;

	public DebDownloadController(SBuildServer sBuildServer, WebControllerManager webControllerManager, 
								 AuthorizationInterceptor authorizationInterceptor, DebRepositoryManager debRepositoryManager) {
		super(sBuildServer);
		webControllerManager.registerController(DEBREPO_BASE_URL_WITH_WILDCARD, this);
		authorizationInterceptor.addPathNotRequiringAuth(DEBREPO_BASE_URL_WITH_WILDCARD);
		this.myDebRepositoryManager = debRepositoryManager;
	}
		
	@Override
	protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String uriPath = request.getPathInfo();
		Matcher matcher = packagesGzPattern.matcher(uriPath);
		if (matcher.matches()) {
			String repoName = matcher.group(1);
			try {
				DebPackageStore store = myDebRepositoryManager.getPackageStore(repoName);
				return servePackagesGzFile(store);
			} catch (NonExistantRepositoryException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return simpleView("Not Found: No Deb Repository exists with the name: " + repoName);
			}
		}
		
		matcher = packagesPattern.matcher(uriPath);
		if (matcher.matches()) {
			String repoName = matcher.group(1);
			try {
				DebPackageStore store = myDebRepositoryManager.getPackageStore(repoName);
				return servePackagesFile(store);
			} catch (NonExistantRepositoryException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return simpleView("Not Found: No Deb Repository exists with the name: " + repoName);
			}
		}
		
		matcher = packagePattern.matcher(uriPath);
		if (matcher.matches()) {
			String repoName = matcher.group(1);
			String arch = matcher.group(2);
			String filename = matcher.group(3);
			try {
				DebPackageStore store = myDebRepositoryManager.getPackageStore(repoName);
				store.findByFilename(arch, filename);
				return servePackagesFile(store);
			} catch (DebPackageNotFoundInStoreException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return simpleView("Not Found: No Deb file found in repository: " + request.getPathInfo());
			} catch (NonExistantRepositoryException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return simpleView("Not Found: No Deb Repository exists with the name: " + repoName);
			}			
		}
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
		return simpleView("Package Not found");
	}

	private ModelAndView servePackagesGzFile(DebPackageStore store) {
		return simpleView ("FIXME: Serving Packages.gz for repo " + store.getUuid());
	}
	
	private ModelAndView servePackagesFile(DebPackageStore store) {
		return simpleView ("FIXME: Serving Packages for repo " + store.getUuid());
	}

}
