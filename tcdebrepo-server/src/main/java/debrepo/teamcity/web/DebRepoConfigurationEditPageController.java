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
package debrepo.teamcity.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.util.StringUtils;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.AuthUtil;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

public class DebRepoConfigurationEditPageController extends BaseController {

	private final PluginDescriptor myPluginDescriptor;
	private final DebRepositoryManager myDebRepositoryManager;
	private final DebRepositoryConfigurationManager myDebRepositoryConfigurationManager;
	private final SBuildServer myBuildServer;
	private SecurityContext mySecurityContext;

	public DebRepoConfigurationEditPageController(@NotNull final SBuildServer buildServer,
			@NotNull final PluginDescriptor pluginDescriptor, @NotNull final WebControllerManager manager,
			@NotNull final DebRepositoryManager debRepositoryManager,
			@NotNull final DebRepositoryConfigurationManager debRepositoryConfigurationManager,
			@NotNull final SecurityContext securityContext) {
		super(buildServer);
		myBuildServer = buildServer;
		myPluginDescriptor = pluginDescriptor;
		myDebRepositoryManager = debRepositoryManager;
		myDebRepositoryConfigurationManager = debRepositoryConfigurationManager;
		mySecurityContext = securityContext;
		manager.registerController("/admin/editDebianRepository.html", this);
	}

	@Override
	protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) {
		DebRepositoryConfiguration repoConfig = getDebRepoFromRequest(request);
		if (repoConfig == null) {
			getOrCreateMessages(request).addMessage("debianRepositoryNotFound",
					"Selected Debian Repository no longer exists");
			return new ModelAndView(new RedirectView("/admin/debianRepositories.html", true));
		}
		ModelAndView mv = new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/editDebianRepository.jsp"));
		SProject myProject = myBuildServer.getProjectManager().findProjectById(repoConfig.getProjectId());
		EditDebRepositoryBean bean = EditDebRepositoryBean.build(myBuildServer.getProjectManager(), repoConfig, myProject);
		bean.getCameFromSupport().setUrlFromRequest(request, "/admin/editProject.html?projectId=" + myProject.getExternalId() + "&tab=tcdebrepo");
		bean.getCameFromSupport().setTitleFromRequest(request, "Priority Classes");
		mv.getModel().put("repoConfig", repoConfig);
		mv.getModel().put("debRepoBean", bean);
		mv.getModel().put("sortedProjects", getPermissionedProjects());
		mv.getModel().put("sortedProjectBuildTypes", myProject.getBuildTypes());

		mv.getModel().put("repoStats", myDebRepositoryManager.getRepositoryStatistics(
											repoConfig, 
        									StringUtils.getDebRepoUrl(
        											myServer.getRootUrl(), 
        											repoConfig.getRepoName(),
        											repoConfig.isRestricted()
        											)
        									)
        							);
		return mv;
	}

	private List<SProject> getPermissionedProjects() {
		List<SProject> usersProjects = new ArrayList<>();
		for (SProject p : myBuildServer.getProjectManager().getProjects()) {
			if (AuthUtil.hasPermissionToManageProject(mySecurityContext.getAuthorityHolder(), p.getProjectId())) {
				usersProjects.add(p);
			}
		}
		return usersProjects;
	}

	@Nullable
	private DebRepositoryConfiguration getDebRepoFromRequest(final HttpServletRequest request) {
		String debRepoName = request.getParameter("repo");
		if (debRepoName != null) {
			return myDebRepositoryConfigurationManager.getDebRepositoryConfigurationByName(debRepoName);
		} else {
			return null;
		}
	}

}
