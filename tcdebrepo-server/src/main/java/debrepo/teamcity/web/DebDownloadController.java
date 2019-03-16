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
import debrepo.teamcity.service.DebReleaseFileLocator;
import debrepo.teamcity.service.DebReleaseFileLocator.PackagesFileType;
import debrepo.teamcity.service.DebReleaseFileLocator.ReleaseFileType;
import debrepo.teamcity.service.DebRepositoryItemNotFoundException;
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

public abstract class DebDownloadController extends BaseController {
	
	private static final String LINK_TYPE_REPO_FILE = "repositoryFile";
	private static final String LINK_TYPE_REPO_DIR = "repositoryDirectory";
	private static final String LINK_TYPE_REP_DIR_SLASH = "repositoryDirectorySeparator";
	
	public static final String DEBREPO_URL_PART_UNRESTRICTED = "/debrepo";
	public static final String DEBREPO_URL_PART_RESTRICTED = "/debrepo-restricted";
	
	public static final String DEBREPO_BASE_URL_UNRESTRICTED = "/app" + DEBREPO_URL_PART_UNRESTRICTED;
	public static final String DEBREPO_BASE_URL_RESTRICTED = "/app" + DEBREPO_URL_PART_RESTRICTED;
	
	public static final String DEBREPO_BASE_URL_FOR_REDIRECT_UNRESTRICTED = "/app" + DEBREPO_URL_PART_UNRESTRICTED;
	public static final String DEBREPO_BASE_URL_FOR_REDIRECT_RESTRICTED = "/httpAuth/app" + DEBREPO_URL_PART_RESTRICTED;
	
	public static final String DEBREPO_BASE_URL_UNRESTRICTED_WITH_WILDCARD = DEBREPO_BASE_URL_UNRESTRICTED + "/**";
	public static final String DEBREPO_BASE_URL_RESTRICTED_WITH_WILDCARD = DEBREPO_BASE_URL_RESTRICTED + "/**";
	
	/**                                                             /debrepo/{RepoName}/dists/{Distribution}/{Component}/binary-{Arch}/Packages	or Packages.gz		*/
	final private Pattern packagesPattern        = Pattern.compile("^" + getDebRepoUrlPart() + "/(\\S+?)/dists/(\\S+?)/(\\S+?)/binary-(\\S+?)/(Packages|Packages\\.gz)$");
	
	/**                                                             /debrepo/{RepoName}/dists/{Distribution}/{Component}/binary-{Arch}/Release			*/
	final private Pattern releaseFileSmallPattern= Pattern.compile("^" + getDebRepoUrlPart() + "/(\\S+?)/dists/(\\S+?)/(\\S+?)/binary-(\\S+?)/Release");
	
	/**                                                             /debrepo/{RepoName}/dists/{Distribution}/{Component}/binary-{Arch}/		            */
	final private Pattern browseArchPattern      = Pattern.compile("^" + getDebRepoUrlPart() + "/(\\S+?)/dists/(\\S+?)/(\\S+?)/binary-(\\S+?)/$");
	
	/**                                                             /debrepo/{RepoName}/dists/{Distribution}/Release or InRelease or Release.gpg       	*/
	final private Pattern releaseFilePattern 	 = Pattern.compile("^" + getDebRepoUrlPart() + "/(\\S+?)/dists/(\\S+?)/(Release|InRelease|Release\\.gpg)$");
	
	/**                                                             /debrepo/{RepoName}/dists/{Distribution}/{Component}/		                        */
	final private Pattern browseComponentPattern = Pattern.compile("^" + getDebRepoUrlPart() + "/(\\S+?)/dists/(\\S+?)/(\\S+?)/$");
	
	/**                                                             /debrepo/{RepoName}/dists/{Distribution}/		                                    */
	final private Pattern browseDistPattern      = Pattern.compile("^" + getDebRepoUrlPart() + "/(\\S+?)/dists/(\\S+?)/$");
	
	/**                                                             /debrepo/{RepoName}/dists/		                                                    */
	final private Pattern browseRepoDistPattern  = Pattern.compile("^" + getDebRepoUrlPart() + "/(\\S+?)/dists/$");
	
	/**                                                             /debrepo/{RepoName}/pool/{Component}/{packageName}/		                            */
	final private Pattern browsePoolPackagePat   = Pattern.compile("^" + getDebRepoUrlPart() + "/(\\S+?)/pool/(\\S+?)/(\\S+?)/");
	
	/**                                                             /debrepo/{RepoName}/pool/{Component}/{packageName/packageFile}		                */
    final private Pattern packageFilenamePattern = Pattern.compile("^" + getDebRepoUrlPart() + "/(\\S+?)/pool/(\\S+?)/(.+)");

    /**                                                             /debrepo/{RepoName}/pool/{Component}/		                                        */
    final private Pattern browsePoolComponentPat = Pattern.compile("^" + getDebRepoUrlPart() + "/(\\S+?)/pool/(\\S+?)/");

    /**                                                             /debrepo/{RepoName}/pool/    		                                                */
    final private Pattern browsePoolPattern      = Pattern.compile("^" + getDebRepoUrlPart() + "/(\\S+?)/pool/");
    
    /**                                                             /debrepo/{RepoName}                    		                                        */
    final private Pattern infoPattern            = Pattern.compile("^" + getDebRepoUrlPart() + "/(\\S+?)/$");
    
    protected final DebRepositoryManager myDebRepositoryManager;
    private final DebReleaseFileLocator myDebReleaseFileLocator;
    private final PluginDescriptor myPluginDescriptor;

	public DebDownloadController(SBuildServer sBuildServer, WebControllerManager webControllerManager, 
								 @NotNull PluginDescriptor descriptor,
								 AuthorizationInterceptor authorizationInterceptor, 
								 DebRepositoryManager debRepositoryManager,
								 DebReleaseFileLocator debReleaseFileLocator) {
		super(sBuildServer);
		configureUrlAndAuthorisation(webControllerManager, authorizationInterceptor);
		this.myDebRepositoryManager = debRepositoryManager;
		this.myDebReleaseFileLocator = debReleaseFileLocator;
		this.myPluginDescriptor = descriptor;
	}
	

	/**
	 * <p>This method returns the correct controller path prefix for the 
	 * relevant controller. This should be provided by the concrete impl
	 * because it knows it's own path prefix</p>
	 *   
	 * @return /debrepo or /debrepo-restricted 
	 */
	protected abstract String getDebRepoUrlPart();
	
	/**
	 * <p>This method is used to build the breadcrumb URLs.
	 * For some reason, when /httpAuth is prepended to the URL,
	 * the request.getServletPath() method returns a string prefixed 
	 * with /httpAuth rather than /httpAuth/app</p>
	 * 
	 * <p>Therefore, for the restricted controller, we need to build
	 * a URL with that re-instated.</p>
	 * 
	 * <p>For the unrestricted controller, just return the string not prepended with /app</p>
	 * 
	 * @return A string like /debrepo or /app/debrepo-restricted (see above for explanation)
	 *    
	 */
	protected abstract String getDebRepoUrlPartWithContext();

	protected abstract void checkRepoIsRestricted(String repoName) throws DebRepositoryAccessIsRestrictedException,
			DebRepositoryPermissionDeniedException, NonExistantRepositoryException;
	
	protected abstract void configureUrlAndAuthorisation(WebControllerManager webControllerManager,
			AuthorizationInterceptor authorizationInterceptor) ;

		
	@Override
	protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String uriPath = request.getPathInfo();
		if (uriPath.startsWith(DEBREPO_BASE_URL_UNRESTRICTED)){
			uriPath = uriPath.substring(4); // "/app" off the front.
		}
		final Map<String,Object> params = new HashMap<String,Object>();
		
		params.put("pluginName", this.myPluginDescriptor.getPluginName());
		params.put("pluginVersion", this.myPluginDescriptor.getPluginVersion());
		params.put("jspHome", this.myPluginDescriptor.getPluginResourcesPath());
		
		/* /debrepo/{RepoName}/dists/{Distribution}/{Component}/binary-{Arch}/Release */
		Matcher matcher = releaseFileSmallPattern.matcher(uriPath);
		if (matcher.matches()) {
			String repoName = matcher.group(1);
			String distName = matcher.group(2);
			String component= matcher.group(3);
			String archName = matcher.group(4);
			try {
				return serveReleaseFile(request, response, repoName, distName, component, archName, ReleaseFileType.Release);
			} catch (NonExistantRepositoryException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				Loggers.SERVER.info("DebDownloadController:: Returning 404 : Not Found: No Deb Repository exists with the name: " + request.getPathInfo());
				return null;
			} catch (DebRepositoryItemNotFoundException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				Loggers.SERVER.info("DebDownloadController:: Returning 404 : Not Found: No Deb Repository Release file exists with the name: " + request.getPathInfo());
				return null;
			}
		}
		
		/* /debrepo/{RepoName}/dists/{Distribution}/{Component}/binary-{Arch}/Packages */
		matcher = packagesPattern.matcher(uriPath);
		if (matcher.matches()) {
			String repoName = matcher.group(1);
			String distName = matcher.group(2);
			String component= matcher.group(3);
			String archName = matcher.group(4);
			String packagesFileName = matcher.group(5);
			try {
				checkRepoIsRestricted(repoName);
				PackagesFileType packagesFileType = PackagesFileType.findByName(packagesFileName);
				return servePackagesFile(request, response, repoName, distName, component, archName, packagesFileType);
				//return servePackagesFile(request, response, myDebRepositoryManager.findAllByDistComponentArchIncludingAll(repoName, distName, component, archName));
			} catch (DebRepositoryPermissionDeniedException ex){
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				Loggers.SERVER.info("DebDownloadController:: Returning 403 : Deb Repository is restricted and user is not permissioned on project: " + request.getPathInfo());
				return null;			
			} catch (DebRepositoryAccessIsRestrictedException ex){
				response.sendRedirect(buildRedirectToRestrictedUrl(request, uriPath));
				Loggers.SERVER.info("DebDownloadController:: Returning 302 : Deb Repository is restricted: " + request.getPathInfo());
				return null;
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
				checkRepoIsRestricted(repoName);
				if (myDebRepositoryManager.findAllByDistComponentArchIncludingAll(repoName, distName, component, archName).size() > 0) {
					List<LinkItem> breadcrumbItems = new ArrayList<>();
					breadcrumbItems.add(LinkItem.builder().text("<b>Index of</b> ").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
					breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
					breadcrumbItems.add(LinkItem.builder().text(repoName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext() + "/" + repoName + "/").build());
					breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
					breadcrumbItems.add(LinkItem.builder().text("dists").type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext() + "/" + repoName + "/dists/").build());
					breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
					breadcrumbItems.add(LinkItem.builder().text(distName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext() + "/" + repoName + "/dists/" + distName + "/").build());
					breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
					breadcrumbItems.add(LinkItem.builder().text(component).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext() + "/" + repoName + "/dists/" + distName + "/" + component + "/").build());
					breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
					breadcrumbItems.add(LinkItem.builder().text("binary-" + archName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext() + "/" + repoName + "/dists/" + distName + "/" + component + "/binary-" + archName + "/").build());
					breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
					params.put("breadcrumbItems", breadcrumbItems);
					
					List<LinkItem> linkItems = new ArrayList<>();
					linkItems.add(LinkItem.builder().text("Packages").type(LINK_TYPE_REPO_FILE).url("./Packages").build());
					linkItems.add(LinkItem.builder().text("Packages.gz").type(LINK_TYPE_REPO_FILE).url("./Packages.gz").build());
					linkItems.add(LinkItem.builder().text("Release").type(LINK_TYPE_REPO_FILE).url("./Release").build());
					linkItems.add(LinkItem.builder().text("by-hash").type(LINK_TYPE_REPO_DIR).url("./by-hash/").build());

					params.put("linkItems", linkItems);
					params.put("directoryTitle", repoName);
					params.put("currentPathLevel", "binary-" + archName);
					return new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/directoryListing.jsp"), params);
				}
			} catch (DebRepositoryPermissionDeniedException ex){
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				Loggers.SERVER.info("DebDownloadController:: Returning 403 : Deb Repository is restricted and user is not permissioned on project: " + request.getPathInfo());
				return null;			
			} catch (DebRepositoryAccessIsRestrictedException ex){
				response.sendRedirect(buildRedirectToRestrictedUrl(request, uriPath));
				Loggers.SERVER.info("DebDownloadController:: Returning 302 : Deb Repository is restricted: " + request.getPathInfo());
				return null;
			} catch (NonExistantRepositoryException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				Loggers.SERVER.info("DebDownloadController:: Returning 404 : Not Found: No Deb Repository exists with the name: " + request.getPathInfo());
				return null;
			}
		}
		
		/* /debrepo/{RepoName}/dists/{Distribution}/{Release} */
		matcher = releaseFilePattern.matcher(uriPath);
		if (matcher.matches()) {
			String repoName = matcher.group(1);
			String distName = matcher.group(2);
			String releaseFileName= matcher.group(3);
			try {
				checkRepoIsRestricted(repoName);
				ReleaseFileType releaseFileType = ReleaseFileType.valueOf(releaseFileName);
				return serveReleaseFile(request, response, repoName, distName, releaseFileType);
			} catch (DebRepositoryPermissionDeniedException ex){
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				Loggers.SERVER.info("DebDownloadController:: Returning 403 : Deb Repository is restricted and user is not permissioned on project: " + request.getPathInfo());
				return null;			
			} catch (DebRepositoryAccessIsRestrictedException ex){
				response.sendRedirect(buildRedirectToRestrictedUrl(request, uriPath));
				Loggers.SERVER.info("DebDownloadController:: Returning 302 : Deb Repository is restricted: " + request.getPathInfo());
				return null;				
			} catch (NonExistantRepositoryException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				Loggers.SERVER.info("DebDownloadController:: Returning 404 : Not Found: No Deb Repository exists with the name: " + request.getPathInfo());
				return null;
			} catch (DebRepositoryItemNotFoundException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				Loggers.SERVER.info("DebDownloadController:: Returning 404 : Not Found: No Deb Repository Release file exists with the name: " + request.getPathInfo());
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
				checkRepoIsRestricted(repoName);
				List<LinkItem> linkItems = new ArrayList<>();
				for (String arch : myDebRepositoryManager.findUniqueArchByDistAndComponent(repoName, distName, component)) {
					linkItems.add(LinkItem.builder().text("binary-" + arch).type(LINK_TYPE_REPO_DIR).url("./binary-" + arch + "/").build());
				}
				params.put("linkItems", linkItems);
				params.put("alertInfo", "deb  " + StringUtils.getDebRepoUrlWithUserPassExample(myServer.getRootUrl(), repoName, myDebRepositoryManager.isRestrictedRepository(repoName)) + "  " + distName + "  " + component);
				params.put("directoryTitle", repoName);
				params.put("currentPathLevel", component);
				
				List<LinkItem> breadcrumbItems = new ArrayList<>();
				breadcrumbItems.add(LinkItem.builder().text("<b>Index of</b> ").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(repoName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext() + "/" + repoName + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("dists").type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext() + "/" + repoName + "/dists/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(distName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext() + "/" + repoName + "/dists/" + distName + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(component).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext() + "/" + repoName + "/dists/" + distName + "/" + component + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				params.put("breadcrumbItems", breadcrumbItems);
				return new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/directoryListing.jsp"), params);
			} catch (DebRepositoryPermissionDeniedException ex){
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				Loggers.SERVER.info("DebDownloadController:: Returning 403 : Deb Repository is restricted and user is not permissioned on project: " + request.getPathInfo());
				return null;			
			} catch (DebRepositoryAccessIsRestrictedException ex){
				response.sendRedirect(buildRedirectToRestrictedUrl(request, uriPath));
				Loggers.SERVER.info("DebDownloadController:: Returning 302 : Deb Repository is restricted: " + request.getPathInfo());
				return null;
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
				checkRepoIsRestricted(repoName);
				List<LinkItem> linkItems = new ArrayList<>();
				linkItems.add(LinkItem.builder().text("InRelease").type(LINK_TYPE_REPO_FILE).url("./InRelease").build());
				linkItems.add(LinkItem.builder().text("Release").type(LINK_TYPE_REPO_FILE).url("./Release").build());
				linkItems.add(LinkItem.builder().text("Release.gpg").type(LINK_TYPE_REPO_FILE).url("./Release.gpg").build());
				for (String component : myDebRepositoryManager.findUniqueComponentByDist(repoName, distName)) {
					linkItems.add(LinkItem.builder().text(component).type(LINK_TYPE_REPO_DIR).url("./" + component + "/").build());
				}
				params.put("linkItems", linkItems);
				params.put("directoryTitle", repoName);
				params.put("currentPathLevel", distName);
				
				List<LinkItem> breadcrumbItems = new ArrayList<>();
				breadcrumbItems.add(LinkItem.builder().text("<b>Index of</b> ").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(repoName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext() + "/" + repoName + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("dists").type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext() + "/" + repoName + "/dists/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(distName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext() + "/" + repoName + "/dists/" + distName + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				params.put("breadcrumbItems", breadcrumbItems);
				return new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/directoryListing.jsp"), params);
			} catch (DebRepositoryPermissionDeniedException ex){
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				Loggers.SERVER.info("DebDownloadController:: Returning 403 : Deb Repository is restricted and user is not permissioned on project: " + request.getPathInfo());
				return null;			
			} catch (DebRepositoryAccessIsRestrictedException ex){
				response.sendRedirect(buildRedirectToRestrictedUrl(request, uriPath));
				Loggers.SERVER.info("DebDownloadController:: Returning 302 : Deb Repository is restricted: " + request.getPathInfo());
				return null;
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
				checkRepoIsRestricted(repoName);
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
				breadcrumbItems.add(LinkItem.builder().text(repoName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext() + "/" + repoName + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("dists").type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext() + "/" + repoName + "/dists/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				params.put("breadcrumbItems", breadcrumbItems);
				return new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/directoryListing.jsp"), params);
			} catch (DebRepositoryPermissionDeniedException ex){
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				Loggers.SERVER.info("DebDownloadController:: Returning 403 : Deb Repository is restricted and user is not permissioned on project: " + request.getPathInfo());
				return null;			
			} catch (DebRepositoryAccessIsRestrictedException ex){
				response.sendRedirect(buildRedirectToRestrictedUrl(request, uriPath));
				Loggers.SERVER.info("DebDownloadController:: Returning 302 : Deb Repository is restricted: " + request.getPathInfo());
				return null;
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
				checkRepoIsRestricted(repoName);
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
				breadcrumbItems.add(LinkItem.builder().text(repoName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext() + "/" + repoName + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("pool").type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext()  + "/" + repoName + "/pool/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(component).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext() + "/" + repoName + "/pool/" + component + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(packageName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext() + "/" + repoName + "/pool/" + component + "/"+ packageName + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				params.put("breadcrumbItems", breadcrumbItems);
				return new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/directoryListing.jsp"), params);				
			} catch (DebRepositoryPermissionDeniedException ex){
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				Loggers.SERVER.info("DebDownloadController:: Returning 403 : Deb Repository is restricted and user is not permissioned on project: " + request.getPathInfo());
				return null;			
			} catch (DebRepositoryAccessIsRestrictedException ex){
				response.sendRedirect(buildRedirectToRestrictedUrl(request, uriPath));
				Loggers.SERVER.info("DebDownloadController:: Returning 302 : Deb Repository is restricted: " + request.getPathInfo());
				return null;
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
				checkRepoIsRestricted(repoName);
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
				breadcrumbItems.add(LinkItem.builder().text(repoName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext() + "/" + repoName + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("pool").type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext()  + "/" + repoName + "/pool/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(component).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext() + "/" + repoName + "/pool/" + component + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				params.put("breadcrumbItems", breadcrumbItems);
				return new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/directoryListing.jsp"), params);				
			} catch (DebRepositoryPermissionDeniedException ex){
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				Loggers.SERVER.info("DebDownloadController:: Returning 403 : Deb Repository is restricted and user is not permissioned on project: " + request.getPathInfo());
				return null;			
			} catch (DebRepositoryAccessIsRestrictedException ex){
				response.sendRedirect(buildRedirectToRestrictedUrl(request, uriPath));
				Loggers.SERVER.info("DebDownloadController:: Returning 302 : Deb Repository is restricted: " + request.getPathInfo());
				return null;
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
				checkRepoIsRestricted(repoName);
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
				breadcrumbItems.add(LinkItem.builder().text(repoName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext() + "/" + repoName + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("pool").type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext()  + "/" + repoName + "/pool/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				params.put("breadcrumbItems", breadcrumbItems);
				return new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/directoryListing.jsp"), params);						
			} catch (DebRepositoryPermissionDeniedException ex){
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				Loggers.SERVER.info("DebDownloadController:: Returning 403 : Deb Repository is restricted and user is not permissioned on project: " + request.getPathInfo());
				return null;			
			} catch (DebRepositoryAccessIsRestrictedException ex){
				response.sendRedirect(buildRedirectToRestrictedUrl(request, uriPath));
				Loggers.SERVER.info("DebDownloadController:: Returning 302 : Deb Repository is restricted: " + request.getPathInfo());
				return null;
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
				checkRepoIsRestricted(repoName);
				uri = uriPath.substring(getDebRepoUrlPart().length() + 1 + repoName.length() + 1);
				DebPackage debPackage = myDebRepositoryManager.findByUri(repoName, uri);
				SBuild build = this.myServer.findBuildInstanceById(debPackage.getBuildId());
				return servePackage(request, response, new File(build.getArtifactsDirectory() + File.separator + debPackage.getFilename()));
			} catch (DebPackageNotFoundInStoreException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				Loggers.SERVER.info("DebDownloadController:: Returning 404 : Not Found: No Deb file found in repository: " + request.getPathInfo());
				Loggers.SERVER.debug(ex);
				return null;
			} catch (DebRepositoryPermissionDeniedException ex){
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				Loggers.SERVER.info("DebDownloadController:: Returning 403 : Deb Repository is restricted and user is not permissioned on project: " + request.getPathInfo());
				return null;			
			} catch (DebRepositoryAccessIsRestrictedException ex){
				response.sendRedirect(buildRedirectToRestrictedUrl(request, uriPath));
				Loggers.SERVER.info("DebDownloadController:: Returning 302 : Deb Repository is restricted: " + request.getPathInfo());
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
			try {
				checkRepoIsRestricted(repoName);
				List<LinkItem> linkItems = new ArrayList<>();
				linkItems.add(LinkItem.builder().text("dists").type(LINK_TYPE_REPO_DIR).url("./dists/").build());
				linkItems.add(LinkItem.builder().text("pool").type(LINK_TYPE_REPO_DIR).url("./pool/").build());
				params.put("linkItems", linkItems);
				params.put("directoryTitle", repoName);
				
				List<LinkItem> breadcrumbItems = new ArrayList<>();
				breadcrumbItems.add(LinkItem.builder().text("<b>Index of</b> ").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				breadcrumbItems.add(LinkItem.builder().text(repoName).type(LINK_TYPE_REPO_DIR).url(request.getServletPath() + getDebRepoUrlPartWithContext() + "/" + repoName + "/").build());
				breadcrumbItems.add(LinkItem.builder().text("/").type(LINK_TYPE_REP_DIR_SLASH).url("").build());
				params.put("breadcrumbItems", breadcrumbItems);
				Loggers.SERVER.info("DebDownloadController:: Returning 200: " + request.getPathInfo() + " Comparing to " + infoPattern + " Class: " + this.getClass().getName());
				return new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/directoryListing.jsp"), params);
			
			} catch (DebRepositoryPermissionDeniedException ex){
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				Loggers.SERVER.info("DebDownloadController:: Returning 403 : Deb Repository is restricted and user is not permissioned on project: " + request.getPathInfo());
				return null;			
			} catch (DebRepositoryAccessIsRestrictedException ex){
				response.sendRedirect(buildRedirectToRestrictedUrl(request, uriPath));
				Loggers.SERVER.info("DebDownloadController:: Returning 302 : Deb Repository is restricted: " + request.getPathInfo());
				return null;			
			} catch (NonExistantRepositoryException ex){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				Loggers.SERVER.info("DebDownloadController:: Returning 404 : Not Found: No Deb Repository exists with the name: " + request.getPathInfo());
				Loggers.SERVER.debug(ex);
				return null;
			}

		}
		
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
		Loggers.SERVER.info("DebDownloadController:: Returning 404 : All regex tried: " + request.getPathInfo() + " Comparing to " + infoPattern + " Class: " + this.getClass().getName());
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
     * @param reponame - The reponame to match against.
     * @param dist - The dist to match against.
     * @param component  -  The component to match against.
     * @param architecture - The architecture to match against.
     * @return null - The actual output it written directly to the response, so the ModelAndView returned is just null.
	 * @throws IOException - If the output stream can't be written to.
	 * @throws NonExistantRepositoryException - If the reponame passed in is not a valid repo.
	 * @throws DebRepositoryItemNotFoundException - If the query to find the packages file returns nothing.
     */
	private ModelAndView servePackagesFile(HttpServletRequest request, HttpServletResponse response, String reponame, 
											 String dist, String component, String architecture, PackagesFileType packagesFileType) 
													 throws IOException, NonExistantRepositoryException, 
													 		DebRepositoryItemNotFoundException {
		response.setContentType(packagesFileType.getContentType());
		response.getOutputStream().write(myDebReleaseFileLocator.findPackagesFile(reponame, packagesFileType, dist, component, architecture));
		Loggers.SERVER.info("DebDownloadController:: Returning 200 : " + packagesFileType.getFilename() + " file exists with the name: " + request.getPathInfo());
		return null;
	}
	
	/**
	 * Requests the "Simple" Release file from the Repository Store.
	 * @param request - The {@link HttpServletRequest} object the page was requested with. For use in 304 handling and similar.
	 * @param response -  The {@link HttpServletResponse} object the response it written to. 
	 * @param reponame - Repo name to request Release file for.
	 * @param dist - Distribution to request Release file for.
	 * @param component - Component to request Release file for.
	 * @param architecture - Architecture to request Release file for.
	 * @return null - The actual output it written directly to the response, so the ModelAndView returned is just null.
	 * @throws IOException - If the output stream can't be written to.
	 * @throws NonExistantRepositoryException - If the reponame passed in is not a valid repo.
	 * @throws DebRepositoryItemNotFoundException - If the query to find the release file returns nothing.
	 */
	private ModelAndView serveReleaseFile(HttpServletRequest request, HttpServletResponse response, String reponame, String dist, String component, String architecture, ReleaseFileType releaseFileType) throws IOException, NonExistantRepositoryException, DebRepositoryItemNotFoundException {
		response.getOutputStream().write(myDebReleaseFileLocator.findReleaseFile(reponame, dist, component, architecture, releaseFileType).getBytes(StandardCharsets.UTF_8));
		Loggers.SERVER.info("DebDownloadController:: Returning 200 : Release file exists with the name: " + request.getPathInfo());
		return null;
	}
	
	/**
	 * Requests the large Release file from the Repository Store.
	 * @param request - The {@link HttpServletRequest} object the page was requested with. For use in 304 handling and similar.
	 * @param response -  The {@link HttpServletResponse} object the response it written to. 
	 * @param reponame - Repo name to request Release file for.
	 * @param dist - Distribution to request Release file for.
	 * @param releaseFileType - {@link ReleaseFileType}. One of Releases, Releases.gpg or InRelease 
	 * @return null - The actual output it written directly to the response, so the ModelAndView returned is just null.
	 * @throws IOException - If the output stream can't be written to.
	 * @throws NonExistantRepositoryException - If the reponame passed in is not a valid repo.
	 * @throws DebRepositoryItemNotFoundException - If the query to find the release file returns nothing.
	 */
	private ModelAndView serveReleaseFile(HttpServletRequest request, HttpServletResponse response, String reponame, String dist, ReleaseFileType releaseFileType) throws IOException, NonExistantRepositoryException, DebRepositoryItemNotFoundException {
		response.getOutputStream().write(myDebReleaseFileLocator.findReleaseFile(reponame, dist, releaseFileType).getBytes(StandardCharsets.UTF_8));
		Loggers.SERVER.info("DebDownloadController:: Returning 200 : Release file exists with the name: " + request.getPathInfo());
		return null;
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
	
	private String buildRedirectToRestrictedUrl(HttpServletRequest request, String uriPath) {
		StringBuilder sb = new StringBuilder();
		sb.append(request.getContextPath())
		  .append(DEBREPO_BASE_URL_FOR_REDIRECT_RESTRICTED)
		  .append(uriPath.substring(getDebRepoUrlPart().length()));
		return sb.toString();
	}
	
	@Data @Builder
	public static class LinkItem {
		String text;
		String url;
		String type;
	}
	
	private static class DebPackageComparator implements Comparator<DebPackage> {
		final int EQUAL = 0;
		
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

