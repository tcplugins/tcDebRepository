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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import debrepo.teamcity.Loggers;
import debrepo.teamcity.entity.DebPackageEntity;
import debrepo.teamcity.entity.DebPackageNotFoundInStoreException;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebRepositoryStatistics;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.NonExistantRepositoryException;
import debrepo.teamcity.util.StringUtils;
import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

public class DebDownloadController extends BaseController {
	
	public static final String DEBREPO_URL_PART = "/debrepo";
	public static final String DEBREPO_BASE_URL = "/app" + DEBREPO_URL_PART;
	private static final String DEBREPO_BASE_URL_WITH_WILDCARD = DEBREPO_BASE_URL + "/**";
	
	/*                                                             /debrepo/{RepoName}/dists/{Distribution}/{Component}/{Arch}/Packages.gz		        */
	final private Pattern packagesGzPattern      = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+)/dists/(\\S+?)/(\\S+?)/(\\S+?)/[Pp]ackages.gz");
	
	/*                                                             /debrepo/{RepoName}/dists/{Distribution}/{Component}/{Arch}/Packages.bz2		        */
	final private Pattern packagesBz2Pattern     = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+?)/dists/(\\S+?)/(\\S+?)/(\\S+?)/[Pp]ackages.bz2");
	
	/*                                                             /debrepo/{RepoName}/dists/{Distribution}/{Component}/{Arch}/Packages		            */
	final private Pattern packagesPattern        = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+?)/dists/(\\S+?)/(\\S+?)/(\\S+?)/[Pp]ackages");
	
	/*                                                             /debrepo/{RepoName}/dists/{Distribution}/{Component}/{Arch}/		                    */
	final private Pattern browseArchPattern      = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+?)/dists/(\\S+?)/(\\S+?)/(\\S+?)/$");
	
	/*                                                             /debrepo/{RepoName}/dists/{Distribution}/{Component}/		                        */
	final private Pattern browseComponentPattern = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+?)/dists/(\\S+?)/(\\S+?)/$");
	
	/*                                                             /debrepo/{RepoName}/dists/{Distribution}/		                                    */
	final private Pattern browseDistPattern      = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+?)/dists/(\\S+?)/(\\S+?)/$");
	
	/*                                                             /debrepo/{RepoName}/pool/{Component}/{packageName}		                                        */
    final private Pattern packageFilenamePattern = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+?)/pool/(\\S+?)/(.+)");
    
    /*                                                             /debrepo/{RepoName}                    		                                        */
    final private Pattern infoPattern            = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+?)$");
    
    private final DebRepositoryManager myDebRepositoryManager;
    private final PluginDescriptor myPluginDescriptor;

	public DebDownloadController(SBuildServer sBuildServer, WebControllerManager webControllerManager, 
								 @NotNull PluginDescriptor descriptor,
								 AuthorizationInterceptor authorizationInterceptor, 
								 DebRepositoryManager debRepositoryManager) {
		super(sBuildServer);
		webControllerManager.registerController(DEBREPO_BASE_URL_WITH_WILDCARD, this);
		authorizationInterceptor.addPathNotRequiringAuth(DEBREPO_BASE_URL_WITH_WILDCARD);
		this.myDebRepositoryManager = debRepositoryManager;
		this.myPluginDescriptor = descriptor;
	}
		
	@Override
	protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String uriPath = request.getPathInfo();
		/* /debrepo/{RepoName}/dists/{Distribution}/{Component}/{Arch}/Packages.gz */
		Matcher matcher = packagesGzPattern.matcher(uriPath);
		if (matcher.matches()) {
			String repoName = matcher.group(1);
			String distName = matcher.group(2);
			String component= matcher.group(3);
			String archName = matcher.group(4);
			try {
				DebPackageStore store = myDebRepositoryManager.getPackageStore(repoName);
				return servePackagesGzFile(response, store.findAllByDistComponentArch(distName, component, archName));
			} catch (NonExistantRepositoryException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return simpleView("Not Found: No Deb Repository exists with the name: " + repoName);
			}
		}
		/* /debrepo/{RepoName}/dists/{Distribution}/{Component}/{Arch}/Packages */
		matcher = packagesPattern.matcher(uriPath);
		if (matcher.matches()) {
			String repoName = matcher.group(1);
			String distName = matcher.group(2);
			String component= matcher.group(3);
			String archName = matcher.group(4);
			try {
				DebPackageStore store = myDebRepositoryManager.getPackageStore(repoName);
				return servePackagesFile(response, store.findAllByDistComponentArch(distName, component, archName));
			} catch (NonExistantRepositoryException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return simpleView("Not Found: No Deb Repository exists with the name: " + repoName);
			}
		}
		
		/* /debrepo/{RepoName}/pool/{packageName} */
		matcher = packageFilenamePattern.matcher(uriPath);
		if (matcher.matches()) {
			String repoName = matcher.group(1);
			String component = matcher.group(2);
			String uri = matcher.group(3);
			try {
				DebPackageStore store = myDebRepositoryManager.getPackageStore(repoName);
				uri = uriPath.substring(DEBREPO_URL_PART.length() + 1 + repoName.length() + 1);
				DebPackageEntity debPackage = store.findByUri(uri);
				SBuild build = this.myServer.findBuildInstanceById(debPackage.getSBuildId());
				return servePackage(response, new File(build.getArtifactsDirectory() + File.separator + debPackage.getFilename()));
			} catch (DebPackageNotFoundInStoreException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return simpleView("Not Found: No Deb file found in repository: " + request.getPathInfo());
			} catch (NonExistantRepositoryException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return simpleView("Not Found: No Deb Repository exists with the name: " + repoName);
			}			
		}
		
		/* /debrepo/{RepoName} */
		matcher = infoPattern.matcher(uriPath);
		if (matcher.matches()) {
			String repoName = matcher.group(1);
			try {
				DebPackageStore store = myDebRepositoryManager.getPackageStore(repoName);
				DebRepositoryStatistics stats = myDebRepositoryManager.getRepositoryStatistics(store.getUuid().toString(), StringUtils.getDebRepoUrl(myServer.getRootUrl(), repoName));
				return serveRepoInfo(store, stats);
			} catch (NonExistantRepositoryException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return simpleView("Not Found: No Deb Repository exists with the name: " + repoName);
			}
		}
		
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
		return simpleView("Package Not found");
	}

	private ModelAndView servePackagesGzFile(HttpServletResponse response, List<DebPackageEntity> packages) {
		/*
		OutputStream os = null; 
		try {
			os = response.getOutputStream();
			Files.copy(packagefile.toPath(), os);
			os.flush();
		} catch (IOException e) {
			Loggers.SERVER.debug(e);
		} finally {
			if (os != null) { os.close(); }
		}
		return null; */
		return simpleView ("FIXME: Serving Packages.gz for repo");
	}
	
	private ModelAndView servePackagesFile(HttpServletResponse response, List<DebPackageEntity> packages) {
		response.setContentType("text/plain");
		final ModelAndView mv = new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/packages.jsp"));
		mv.getModel().put("packages", packages);
		return mv;
	}
	
	private ModelAndView servePackage(HttpServletResponse response, File packagefile) throws IOException {
		response.setContentType("application/octect-stream");
		if (! packagefile.canRead()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		OutputStream os = null; 
		try {
			os = response.getOutputStream();
			Files.copy(packagefile.toPath(), os);
			os.flush();
		} catch (IOException e) {
			Loggers.SERVER.debug(e);
		} finally {
			if (os != null) { os.close(); }
		}
		return null;
	}
	
	private ModelAndView serveRepoInfo(DebPackageStore store, DebRepositoryStatistics stats) {
		return simpleView ("FIXME: Serving Packages for repo " + store.getUuid() + "<br>" + stats);
	}

}

