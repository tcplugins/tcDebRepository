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
import org.springframework.web.servlet.ModelAndView;

import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryStatistics;
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
import lombok.AllArgsConstructor;
import lombok.Value;

public class DebRepoListingPageController extends BaseController {

	private final PluginDescriptor myPluginDescriptor;
	private final DebRepositoryConfigurationManager myDebRepositoryConfigurationManager;
	private final @NotNull DebRepositoryManager myDebRepositoryManager;
	private final SBuildServer myBuildServer;
	private final SecurityContext mySecurityContext;

	public DebRepoListingPageController(@NotNull final SBuildServer buildServer,
			@NotNull final DebRepositoryManager debRepositoryManager,
			@NotNull final SecurityContext securityContext,
			@NotNull final PluginDescriptor pluginDescriptor, @NotNull final WebControllerManager manager,
			@NotNull final DebRepositoryConfigurationManager debRepositoryConfigurationManager) {
		super(buildServer);
		myBuildServer = buildServer;
		myPluginDescriptor = pluginDescriptor;
		myDebRepositoryConfigurationManager = debRepositoryConfigurationManager;
		myDebRepositoryManager = debRepositoryManager;
		mySecurityContext = securityContext;
		manager.registerController("/admin/debianRepositories.html", this);
	}

	@Override
	protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) {
		
        List<DebRepositoryConfigProjectAndStatsWrapper> reposList = getReposList();
		ModelAndView mv = new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/debianRepositories.jsp"));
		mv.getModel().put("repoConfigs", reposList);
		return mv;
	}

	private List<DebRepositoryConfigProjectAndStatsWrapper> getReposList() {
		List<DebRepositoryConfigProjectAndStatsWrapper> configs = new ArrayList<>();
		for (DebRepositoryConfiguration config : myDebRepositoryConfigurationManager.getAllConfigurations()) {
			SProject project = myBuildServer.getProjectManager().findProjectById(config.getProjectId());
			boolean permissioned = AuthUtil.hasPermissionToManageProject(mySecurityContext.getAuthorityHolder(), project.getProjectId());
			configs.add(
					new DebRepositoryConfigProjectAndStatsWrapper(project,
							config, 
							myDebRepositoryManager.getRepositoryStatistics(
						   						config, 
						   						StringUtils.getDebRepoUrl(
						   										myServer.getRootUrl(), 
						   										config.getRepoName())
						   						), 
							permissioned)
					);
		}
		return configs;
	}

	@Value @AllArgsConstructor
	public static class DebRepositoryConfigProjectAndStatsWrapper {
		SProject project;
		DebRepositoryConfiguration debRepositoryConfiguration;
		DebRepositoryStatistics debRepositoryStatistics;
		Boolean permissionedOnProject;
	}

}
