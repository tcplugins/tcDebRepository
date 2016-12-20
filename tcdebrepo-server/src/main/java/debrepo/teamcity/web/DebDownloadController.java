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
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.Loggers;
import debrepo.teamcity.entity.DebPackageNotFoundInStoreException;
import debrepo.teamcity.entity.helper.DebPackageToPackageDescriptionBuilder;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.NonExistantRepositoryException;
import debrepo.teamcity.util.StringUtils;
import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import lombok.Builder;
import lombok.Data;

public class DebDownloadController extends BaseController {
	
	private static final String LINK_TYPE_REPO_FILE = "repositoryFile";
	private static final String LINK_TYPE_REPO_DIR = "repositoryDirectory";
	private static final String LINK_TYPE_REP_DIR_SLASH = "repositoryDirectorySeparator";
	public static final String DEBREPO_URL_PART = "/debrepo";
	public static final String DEBREPO_BASE_URL = "/app" + DEBREPO_URL_PART;
	private static final String DEBREPO_BASE_URL_WITH_WILDCARD = DEBREPO_BASE_URL + "/**";
	
	/**                                                             /debrepo/{RepoName}/dists/{Distribution}/{Component}/binary-{Arch}/Packages.gz		*/
	final private Pattern packagesGzPattern      = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+)/dists/(\\S+?)/(\\S+?)/binary-(\\S+?)/[Pp]ackages.gz");
	
	/**                                                             /debrepo/{RepoName}/dists/{Distribution}/{Component}/{Arch}/Packages.bz2		    */
	final private Pattern packagesBz2Pattern     = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+?)/dists/(\\S+?)/(\\S+?)/(\\S+?)/[Pp]ackages.bz2");
	
	/**                                                             /debrepo/{RepoName}/dists/{Distribution}/{Component}/{Arch}/Packages		        */
	final private Pattern packagesPattern        = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+?)/dists/(\\S+?)/(\\S+?)/binary-(\\S+?)/[Pp]ackages");
	
	/**                                                             /debrepo/{RepoName}/dists/{Distribution}/{Component}/binary-{Arch}/		            */
	final private Pattern browseArchPattern      = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+?)/dists/(\\S+?)/(\\S+?)/binary-(\\S+?)/$");
	
	/**                                                             /debrepo/{RepoName}/dists/{Distribution}/{Component}/		                        */
	final private Pattern browseComponentPattern = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+?)/dists/(\\S+?)/(\\S+?)/$");
	
	/**                                                             /debrepo/{RepoName}/dists/{Distribution}/		                                    */
	final private Pattern browseDistPattern      = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+?)/dists/(\\S+?)/$");
	
	/**                                                             /debrepo/{RepoName}/dists/		                                                    */
	final private Pattern browseRepoDistPattern  = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+?)/dists/$");
	
	/**                                                             /debrepo/{RepoName}/pool/{Component}/{packageName}/		                            */
	final private Pattern browsePoolPackagePat   = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+?)/pool/(\\S+?)/(\\S+?)/");
	
	/**                                                             /debrepo/{RepoName}/pool/{Component}/{packageName/packageFile}		                */
    final private Pattern packageFilenamePattern = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+?)/pool/(\\S+?)/(.+)");

    /**                                                             /debrepo/{RepoName}/pool/{Component}/		                                        */
    final private Pattern browsePoolComponentPat = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+?)/pool/(\\S+?)/");

    /**                                                             /debrepo/{RepoName}/pool/    		                                                */
    final private Pattern browsePoolPattern      = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+?)/pool/");
    
    /**                                                             /debrepo/{RepoName}                    		                                        */
    final private Pattern infoPattern            = Pattern.compile("^" + DEBREPO_URL_PART + "/(\\S+?)/$");
    
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
		final Map<String,Object> params = new HashMap<String,Object>();
		
		params.put("pluginName", this.myPluginDescriptor.getPluginName());
		params.put("pluginVersion", this.myPluginDescriptor.getPluginVersion());
		params.put("jspHome", this.myPluginDescriptor.getPluginResourcesPath());
		
		/* /debrepo/{RepoName}/dists/{Distribution}/{Component}/{Arch}/Packages.gz */
		Matcher matcher = packagesGzPattern.matcher(uriPath);
		if (matcher.matches()) {
			String repoName = matcher.group(1);
			String distName = matcher.group(2);
			String component= matcher.group(3);
			String archName = matcher.group(4);
			try {
				return servePackagesGzFile(request, response, myDebRepositoryManager.findAllByDistComponentArchIncludingAll(repoName, distName, component, archName));
			} catch (NonExistantRepositoryException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				Loggers.SERVER.info("DebDownloadController:: Returning 404 : Not Found: No Deb Repository exists with the name: " + request.getPathInfo());
				return null;
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
				return servePackagesFile(request, response, myDebRepositoryManager.findAllByDistComponentArchIncludingAll(repoName, distName, component, archName));
			} catch (NonExistantRepositoryException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				Loggers.SERVER.info("DebDownloadController:: Returning 404 : Not Found: No Deb Repository exists with the name: " + request.getPathInfo());
				return null;
			}
		}
		
		/* /debrepo/{RepoName}/dists/{Distribution}/{Component}/binary-{Arch}/ */
		matcher = browseArchPattern.matcher(uriPath);
		if (matcher.matches()) {
			String repoName = matcher.group(1);
			String distName = matcher.group(2);
			String component= matcher.group(3);
			String archName = matcher.group(4);
			try {
				if (myDebRepositoryManager.findAllByDistComponentArchIncludingAll(repoName, distName, component, archName).size() > 0) {
					List<LinkItem> breadcrumbItems = new ArrayList<>();
					breadcrumbItems.add(LinkItem.builder().text("<b>Index of</b> ").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
					breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
					breadcrumbItems.add(LinkItem.builder().text(repoName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART + "/" + repoName + "/").build());
					breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
					breadcrumbItems.add(LinkItem.builder().text("dists").type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART + "/" + repoName + "/dists/").build());
					breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
					breadcrumbItems.add(LinkItem.builder().text(distName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART + "/" + repoName + "/dists/" + distName + "/").build());
					breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
					breadcrumbItems.add(LinkItem.builder().text(component).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART + "/" + repoName + "/dists/" + distName + "/" + component + "/").build());
					breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
					breadcrumbItems.add(LinkItem.builder().text("binary-" + archName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART + "/" + repoName + "/dists/" + distName + "/" + component + "/binary-" + archName + "/").build());
					breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
					params.put("breadcrumbItems", breadcrumbItems);
					
					List<LinkItem> linkItems = new ArrayList<>();
					linkItems.add(LinkItem.builder().text("Packages").type(LINK_TYPE_REPO_FILE).url("./Packages").build());
					linkItems.add(LinkItem.builder().text("Packages.gz").type(LINK_TYPE_REPO_FILE).url("./Packages.gz").build());
					params.put("linkItems", linkItems);
					params.put("directoryTitle", repoName);
					params.put("currentPathLevel", "binary-" + archName);
					return new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/directoryListing.jsp"), params);
				}
			} catch (NonExistantRepositoryException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				Loggers.SERVER.info("DebDownloadController:: Returning 404 : Not Found: No Deb Repository exists with the name: " + request.getPathInfo());
				return null;
			}
		}
		
		/* /debrepo/{RepoName}/dists/{Distribution}/{Component}/ */
		matcher = browseComponentPattern.matcher(uriPath);
		if (matcher.matches()) {
			String repoName = matcher.group(1);
			String distName = matcher.group(2);
			String component= matcher.group(3);
			try {
				List<LinkItem> linkItems = new ArrayList<>();
				for (String arch : myDebRepositoryManager.findUniqueArchByDistAndComponent(repoName, distName, component)) {
					linkItems.add(LinkItem.builder().text("binary-" + arch).type(LINK_TYPE_REPO_DIR).url("./binary-" + arch + "/").build());
				}
				params.put("linkItems", linkItems);
				params.put("alertInfo", "deb  " + StringUtils.getDebRepoUrl(myServer.getRootUrl(), repoName) + "  " + distName + "  " + component);
				params.put("directoryTitle", repoName);
				params.put("currentPathLevel", component);
				
				List<LinkItem> breadcrumbItems = new ArrayList<>();
				breadcrumbItems.add(LinkItem.builder().text("<b>Index of</b> ").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(repoName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART + "/" + repoName + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("dists").type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART + "/" + repoName + "/dists/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(distName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART + "/" + repoName + "/dists/" + distName + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(component).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART + "/" + repoName + "/dists/" + distName + "/" + component + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				params.put("breadcrumbItems", breadcrumbItems);
				return new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/directoryListing.jsp"), params);
			} catch (NonExistantRepositoryException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				Loggers.SERVER.info("DebDownloadController:: Returning 404 : Not Found: No Deb Repository exists with the name: " + request.getPathInfo());
				return null;
			}
		}
		
		/* /debrepo/{RepoName}/dists/{Distribution}/ */
		matcher = browseDistPattern.matcher(uriPath);
		if (matcher.matches()) {
			String repoName = matcher.group(1);
			String distName = matcher.group(2);
			try {
				List<LinkItem> linkItems = new ArrayList<>();
				for (String component : myDebRepositoryManager.findUniqueComponentByDist(repoName, distName)) {
					linkItems.add(LinkItem.builder().text(component).type(LINK_TYPE_REPO_DIR).url("./" + component + "/").build());
				}
				params.put("linkItems", linkItems);
				params.put("directoryTitle", repoName);
				params.put("currentPathLevel", distName);
				
				List<LinkItem> breadcrumbItems = new ArrayList<>();
				breadcrumbItems.add(LinkItem.builder().text("<b>Index of</b> ").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(repoName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART + "/" + repoName + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("dists").type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART + "/" + repoName + "/dists/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(distName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART + "/" + repoName + "/dists/" + distName + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				params.put("breadcrumbItems", breadcrumbItems);
				return new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/directoryListing.jsp"), params);
			} catch (NonExistantRepositoryException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				Loggers.SERVER.info("DebDownloadController:: Returning 404 : Not Found: No Deb Repository exists with the name: " + request.getPathInfo());
				return null;
			}
		}
		
		/* /debrepo/{RepoName}/dists/ */
		matcher = browseRepoDistPattern.matcher(uriPath);
		if (matcher.matches()) {
			String repoName = matcher.group(1);
			try {
				List<LinkItem> linkItems = new ArrayList<>();
				for (String dist : myDebRepositoryManager.findUniqueDist(repoName)) {
					linkItems.add(LinkItem.builder().text(dist).type(LINK_TYPE_REPO_DIR).url("./" + dist + "/").build());
				}
				params.put("linkItems", linkItems);
				params.put("directoryTitle", repoName);
				params.put("currentPathLevel", "dist");
				
				List<LinkItem> breadcrumbItems = new ArrayList<>();
				breadcrumbItems.add(LinkItem.builder().text("<b>Index of</b> ").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(repoName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART + "/" + repoName + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("dists").type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART + "/" + repoName + "/dists/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				params.put("breadcrumbItems", breadcrumbItems);
				return new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/directoryListing.jsp"), params);
			} catch (NonExistantRepositoryException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				Loggers.SERVER.info("DebDownloadController:: Returning 404 : Not Found: No Deb Repository exists with the name: " + request.getPathInfo());
				return null;
			}
		}

		
		/* /debrepo/{RepoName}/pool/{Component}/{packageName}/ */
		matcher = browsePoolPackagePat.matcher(uriPath);
		if (matcher.matches()) {
			String repoName = matcher.group(1);
			String component= matcher.group(2);
			String packageName = matcher.group(3);
			try {
				List<LinkItem> linkItems = new ArrayList<>();
				List<? extends DebPackage> debs = myDebRepositoryManager.getUniquePackagesByComponentAndPackageName(repoName, component, packageName);
				Collections.sort(debs, new DebPackageComparator());
				for (DebPackage deb : debs) {
					linkItems.add(LinkItem.builder().text(deb.getFilename()).type(LINK_TYPE_REPO_FILE).url("../../../" + deb.getUri()).build());
				}
				params.put("linkItems", linkItems);
				params.put("directoryTitle", repoName);
				params.put("currentPathLevel", packageName);
				
				List<LinkItem> breadcrumbItems = new ArrayList<>();
				breadcrumbItems.add(LinkItem.builder().text("<b>Index of</b> ").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(repoName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART + "/" + repoName + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("pool").type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART  + "/" + repoName + "/pool/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(component).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART + "/" + repoName + "/pool/" + component + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(packageName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART + "/" + repoName + "/pool/" + component + "/"+ packageName + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				params.put("breadcrumbItems", breadcrumbItems);
				return new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/directoryListing.jsp"), params);				
			} catch (NonExistantRepositoryException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				Loggers.SERVER.info("DebDownloadController:: Returning 404 : Not Found: No Deb Repository exists with the name: " + request.getPathInfo());
				Loggers.SERVER.debug(ex);
				return null;
			}
		}
		/* /debrepo/{RepoName}/pool/{Component}/ */
		matcher = browsePoolComponentPat.matcher(uriPath);
		if (matcher.matches()) {
			String repoName = matcher.group(1);
			String component= matcher.group(2);
			try {
				List<LinkItem> linkItems = new ArrayList<>();
				for (String packageName : myDebRepositoryManager.findUniquePackageNameByComponent(repoName, component)) {
					linkItems.add(LinkItem.builder().text(packageName).type(LINK_TYPE_REPO_FILE).url("./" + packageName + "/").build());
				}
				params.put("linkItems", linkItems);
				params.put("directoryTitle", repoName);
				params.put("currentPathLevel", component);
				
				List<LinkItem> breadcrumbItems = new ArrayList<>();
				breadcrumbItems.add(LinkItem.builder().text("<b>Index of</b> ").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(repoName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART + "/" + repoName + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("pool").type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART  + "/" + repoName + "/pool/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(component).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART + "/" + repoName + "/pool/" + component + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				params.put("breadcrumbItems", breadcrumbItems);
				return new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/directoryListing.jsp"), params);				
			} catch (NonExistantRepositoryException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				Loggers.SERVER.info("DebDownloadController:: Returning 404 : Not Found: No Deb Repository exists with the name: " + request.getPathInfo());
				Loggers.SERVER.debug(ex);
				return null;
			}
		}
		
	    /* /debrepo/{RepoName}/pool/ */
		matcher = browsePoolPattern.matcher(uriPath);
		if (matcher.matches()) {
			String repoName = matcher.group(1);
			try {
				List<LinkItem> linkItems = new ArrayList<>();
				for (String component : myDebRepositoryManager.findUniqueComponent(repoName)) {
					linkItems.add(LinkItem.builder().text(component).type(LINK_TYPE_REPO_FILE).url("./" + component + "/").build());
				}
				params.put("linkItems", linkItems);
				params.put("directoryTitle", repoName);
				params.put("currentPathLevel", "pool");
				
				List<LinkItem> breadcrumbItems = new ArrayList<>();
				breadcrumbItems.add(LinkItem.builder().text("<b>Index of</b> ").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(repoName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART + "/" + repoName + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("pool").type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART  + "/" + repoName + "/pool/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				params.put("breadcrumbItems", breadcrumbItems);
				return new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/directoryListing.jsp"), params);						
			} catch (NonExistantRepositoryException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				Loggers.SERVER.info("DebDownloadController:: Returning 404 : Not Found: No Deb Repository exists with the name: " + request.getPathInfo());
				Loggers.SERVER.debug(ex);
				return null;
			}			
		}
		
		/* /debrepo/{RepoName}/pool/{Component}/{packageName} */
		matcher = packageFilenamePattern.matcher(uriPath);
		if (matcher.matches()) {
			String repoName = matcher.group(1);
			String uri = matcher.group(3);
			try {
				uri = uriPath.substring(DEBREPO_URL_PART.length() + 1 + repoName.length() + 1);
				DebPackage debPackage = myDebRepositoryManager.findByUri(repoName, uri);
				SBuild build = this.myServer.findBuildInstanceById(debPackage.getBuildId());
				return servePackage(request, response, new File(build.getArtifactsDirectory() + File.separator + debPackage.getFilename()));
			} catch (DebPackageNotFoundInStoreException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				Loggers.SERVER.info("DebDownloadController:: Returning 404 : Not Found: No Deb file found in repository: " + request.getPathInfo());
				Loggers.SERVER.debug(ex);
				return null;
			} catch (NonExistantRepositoryException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				Loggers.SERVER.info("DebDownloadController:: Returning 404 : Not Found: No Deb Repository exists with the name: " + request.getPathInfo());
				Loggers.SERVER.debug(ex);
				return null;
			}			
		}

		
		/* /debrepo/{RepoName}/ */
		matcher = infoPattern.matcher(uriPath);
		if (matcher.matches()) {
			String repoName = matcher.group(1);
			if (myDebRepositoryManager.isExistingRepository(repoName)){
				List<LinkItem> linkItems = new ArrayList<>();
				linkItems.add(LinkItem.builder().text("dists").type(LINK_TYPE_REPO_DIR).url("./dists/").build());
				linkItems.add(LinkItem.builder().text("pool").type(LINK_TYPE_REPO_DIR).url("./pool/").build());
				params.put("linkItems", linkItems);
				params.put("directoryTitle", repoName);
				
				List<LinkItem> breadcrumbItems = new ArrayList<>();
				breadcrumbItems.add(LinkItem.builder().text("<b>Index of</b> ").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(repoName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + DEBREPO_URL_PART + "/" + repoName + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				params.put("breadcrumbItems", breadcrumbItems);
				return new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/directoryListing.jsp"), params);
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				Loggers.SERVER.info("DebDownloadController:: Returning 404 : Not Found: No Deb Repository exists with the name: " + request.getPathInfo());
				return null;
			}
		}
		
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
		Loggers.SERVER.info("DebDownloadController:: Returning 404 : All regex tried: " + request.getPathInfo());
		return null;
	}

	/**
	 * Creates a new {@link GZIPOutputStream} and streams the stringToGzip through it to the provided {@link OutputStream}
	 * @param stringToGzip - String of text which needs gzipping.
	 * @param outputStream - {@link OutputStream} to write gzip'd string to.
	 * @throws IOException
	 */
    private void gzip(String stringToGzip, OutputStream outputStream) throws IOException {
        GZIPOutputStream gzip = new GZIPOutputStream(outputStream);
        OutputStreamWriter osw = new OutputStreamWriter(gzip, StandardCharsets.UTF_8);
        osw.write(stringToGzip);
        osw.flush();
        osw.close();
    }	
	
    /**
     * <p>Takes a List of {@link DebPackage}s, builds the Debian Package text file and then serves it as a 
     * GZIP'd compressed stream to the HttpServletResponse OutputStream. </p>
     * <p>Packages.gz is one of the files which apt retrieves when <code>apt-get update</code> is executed
     * on a debian system. It contains the list of packages available.</p>
     * 
     * @param request - The {@link HttpServletRequest} object the page was requested with.
     * @param response - The {@link HttpServletResponse} that GZIP the stream to written to.
     * @param packages - A List of {@link DebPackage} containing the package metadata which is  
     * @return
     * @throws IOException
     */
	private ModelAndView servePackagesGzFile(HttpServletRequest request, HttpServletResponse response, List<? extends DebPackage> packages) throws IOException {
		
		String packagesString = DebPackageToPackageDescriptionBuilder.buildPackageDescriptionList(packages);
		response.setContentType("application/x-gzip");
		gzip(packagesString, response.getOutputStream());
		Loggers.SERVER.info("DebDownloadController:: Returning 200 : Packages.gz file exists with the name: " + request.getPathInfo());
		return null;
	}
	
	private ModelAndView servePackagesFile(HttpServletRequest request, HttpServletResponse response, List<? extends DebPackage> packages) {
		response.setContentType("text/plain");
		final ModelAndView mv = new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/packages.jsp"));
		mv.getModel().put("packages", packages);
		Loggers.SERVER.info("DebDownloadController:: Returning 200 : Packages file exists with the name: " + request.getPathInfo());
		return mv;
	}
	
	private ModelAndView servePackage(HttpServletRequest request, HttpServletResponse response, File packagefile) throws IOException {
		if (! packagefile.canRead()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		response.setContentType("application/octect-stream");
		OutputStream os = null; 
		try {
			os = response.getOutputStream();
			Files.copy(packagefile.toPath(), os);
			os.flush();
			Loggers.SERVER.info("DebDownloadController:: Returning 200 : Debian Package exists with the name: " + request.getPathInfo());
		} catch (IOException e) {
			Loggers.SERVER.debug(e);
		} finally {
			if (os != null) { os.close(); }
		}
		return null;
	}
	
	@Data @Builder
	public static class LinkItem {
		String text;
		String url;
		String type;
	}
	
	private static class DebPackageComparator implements Comparator<DebPackage> {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;
		
		@Override
		public int compare(DebPackage o1, DebPackage o2) {
			
			int comparison = o1.getPackageName().compareToIgnoreCase(o2.getPackageName());
			if (comparison != EQUAL) return comparison;

			comparison = o1.getVersion().compareToIgnoreCase(o2.getVersion());
			if (comparison != EQUAL) return comparison;
			
			comparison = o1.getArch().compareToIgnoreCase(o2.getArch());
			if (comparison != EQUAL) return comparison;
			
			return EQUAL;
		}
		
	}

}

