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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import debrepo.teamcity.settings.DebRepoProjectSettings;
import debrepo.teamcity.settings.DebRepoProjectSettingsPersister;
import debrepo.teamcity.util.StringUtils;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimpleCustomTab;

public class DebRepoProjectSettingsPage extends SimpleCustomTab {
	final ProjectSettingsManager mySettings;
	final SBuildServer myServer;
	
	public DebRepoProjectSettingsPage(@NotNull PagePlaces pagePlaces, @NotNull PluginDescriptor descriptor,
									  @NotNull ProjectSettingsManager settings, @NotNull SBuildServer sBuildServer) {
		super(pagePlaces, PlaceId.EDIT_PROJECT_PAGE_TAB, "debRepository",
				descriptor.getPluginResourcesPath("debRepository/projectConfigTab.jsp"), "Deb Repository");
		this.mySettings = settings;
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
			DebRepoProjectSettingsPersister projectSettings = (DebRepoProjectSettingsPersister) mySettings.getSettings(project.getProjectId(), DebRepoProjectSettings.PROJECT_SETTINGS_KEY);
			model.put("debRepoUrl", StringUtils.getDebRepoUrl(myServer.getRootUrl(), projectSettings.getRepositoryName()));
			model.put("debRepoSettings", projectSettings.getSettings());
		}
	}
}