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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryStatistics;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.util.StringUtils;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimpleCustomTab;

public class DebRepoProjectSettingsPage extends SimpleCustomTab {
	final DebRepositoryManager myDebRepositoryManager;
	final DebRepositoryConfigurationManager myDebRepositoryConfigManager;
	final SBuildServer myServer;
	
	public DebRepoProjectSettingsPage(@NotNull PagePlaces pagePlaces, @NotNull PluginDescriptor descriptor,
									  @NotNull DebRepositoryManager debRepositoryManager, 
									  @NotNull DebRepositoryConfigurationManager debRepositoryConfigManager,
									  @NotNull SBuildServer sBuildServer) {
		super(pagePlaces, PlaceId.EDIT_PROJECT_PAGE_TAB, "debRepository",
				descriptor.getPluginResourcesPath("debRepository/projectConfigTab.jsp"), "Deb Repository");
		this.myDebRepositoryManager = debRepositoryManager;
		this.myDebRepositoryConfigManager = debRepositoryConfigManager;
		this.myServer = sBuildServer;
		register();
	}

	@Override
	public boolean isAvailable(@NotNull HttpServletRequest request) {
		return super.isAvailable(request);
	}

	@Override
	public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
		if (request.getParameter("projectId") != null && ! request.getParameter("projectId").equals("")){
			SProject project = myServer.getProjectManager().findProjectByExternalId(request.getParameter("projectId"));
			
			List<DebRepositoryStatistics> respositories = new ArrayList<>();
			for (DebRepositoryConfiguration config : myDebRepositoryConfigManager.getConfigurationsForProject(project.getProjectId())) {
				respositories.add(myDebRepositoryManager.getRepositoryStatistics(config, StringUtils.getDebRepoUrl(myServer.getRootUrl(), config.getRepoName())));
				
			}
			model.put("repositories", respositories);
		}
	}
}