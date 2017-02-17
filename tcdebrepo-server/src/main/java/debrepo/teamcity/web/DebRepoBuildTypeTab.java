/*******************************************************************************
 * Copyright 2017 Net Wolf UK
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

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.openapi.buildType.BuildTypeTab;

public class DebRepoBuildTypeTab extends BuildTypeTab {
	private static final String TAB_TITLE = "Debian Repositories";
	private DebRepositoryConfigurationManager myDebRepositoryConfigurationManager;

	protected DebRepoBuildTypeTab(
			PagePlaces pagePlaces, ProjectManager projectManager, SecurityContext securityContext, 
			PluginDescriptor pluginDescriptor, WebControllerManager manager, 
			DebRepositoryConfigurationManager debRepositoryConfigurationManager) {
		super(pluginDescriptor.getPluginName(), TAB_TITLE, manager, projectManager, "debRepository/buildTypeTab.jsp");
		this.myDebRepositoryConfigurationManager = debRepositoryConfigurationManager;
	}
	
	@Override
	public String getTabTitle(HttpServletRequest request) {
		SBuildType buildType = getBuildType(request);
        if (buildType == null) {
            return TAB_TITLE;
        }
        final Set<DebRepositoryConfiguration> repoConfigs = myDebRepositoryConfigurationManager.findConfigurationsForBuildType(buildType.getBuildTypeId());
        if (repoConfigs.isEmpty()) {
            return TAB_TITLE;
        }
        return TAB_TITLE + " (" + repoConfigs.size() + ")";
	}

	public boolean isAvailable(@NotNull HttpServletRequest request) {
		return true;
	}

	@Override
	protected void fillModel(Map<String, Object> model, HttpServletRequest request, SBuildType buildType, SUser user) {
		model.put("repoConfigs", myDebRepositoryConfigurationManager.findConfigurationsForBuildType(buildType.getBuildTypeId()));
	}
}
