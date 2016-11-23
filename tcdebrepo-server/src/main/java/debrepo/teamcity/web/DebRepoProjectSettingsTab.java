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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import debrepo.teamcity.entity.DebRepositoryConfigurationJaxImpl;
import debrepo.teamcity.entity.DebRepositoryStatistics;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.util.StringUtils;
import jetbrains.buildServer.controllers.admin.projects.EditProjectTab;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.AuthUtil;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import lombok.AllArgsConstructor;
import lombok.Value;

public class DebRepoProjectSettingsTab extends EditProjectTab {
	private static final String TAB_TITLE = "Deb Repository";
	private final DebRepositoryManager myDebRepositoryManager;
	private final DebRepositoryConfigurationManager myDebRepositoryConfigManager;
	private final SBuildServer myServer;
	private SecurityContext mySecurityContext;
	
	public DebRepoProjectSettingsTab(@NotNull PagePlaces pagePlaces, @NotNull PluginDescriptor descriptor,
									  @NotNull DebRepositoryManager debRepositoryManager,
									  @NotNull DebRepositoryConfigurationManager debRepositoryConfigManager,
									  @NotNull PluginDescriptor pluginDescriptor,
									  @NotNull SecurityContext securityContext,
									  @NotNull SBuildServer sBuildServer) {
		super(pagePlaces, pluginDescriptor.getPluginName(), "debRepository/projectConfigTab.jsp", TAB_TITLE);
		this.myDebRepositoryManager = debRepositoryManager;
		this.myDebRepositoryConfigManager = debRepositoryConfigManager;
        this.mySecurityContext = securityContext;
        this.myServer = sBuildServer;
        addCssFile(pluginDescriptor.getPluginResourcesPath("debRepository/debRepository.css"));
        addJsFile(pluginDescriptor.getPluginResourcesPath("debRepository/projectConfigSettings.js"));
    }

    @NotNull
    @Override
    public String getTabTitle(@NotNull final HttpServletRequest request) {
        final SProject currentProject = getProject(request);
        if (currentProject == null) {
            return TAB_TITLE;
        }
        final List<DebRepositoryConfigurationJaxImpl> repoConfigs = myDebRepositoryConfigManager.getConfigurationsForProject(currentProject.getProjectId());
        if (repoConfigs.isEmpty()) {
            return TAB_TITLE;
        }
        return TAB_TITLE + " (" + repoConfigs.size() + ")";
    }

    @Override
    public void fillModel(@NotNull final Map<String, Object> model, @NotNull final HttpServletRequest request) {
        final SProject currentProject = getProject(request);
        if (currentProject == null) {
            return;
        }
        Map<SProject, List<DebRepositoryConfigAndStatsWrapper>> reposMap = getServersMap(currentProject);
        model.put("repositoriesMap", reposMap);
        model.put("projectId", currentProject.getExternalId());
        model.put("userHasPermissionManagement", AuthUtil.hasPermissionToManageProject(mySecurityContext.getAuthorityHolder(), currentProject.getProjectId()));
    }

    private Map<SProject, List<DebRepositoryConfigAndStatsWrapper>> getServersMap(@NotNull final SProject currentProject) {
        SProject project = currentProject;
        Map<SProject, List<DebRepositoryConfigAndStatsWrapper>> reposMap = new HashMap<SProject, List<DebRepositoryConfigAndStatsWrapper>>();
        while (project != null) {
            if (reposMap.containsKey(project)) {
                break;
            }
            final List<DebRepositoryConfigAndStatsWrapper> infoPack = new ArrayList<>();
            for (DebRepositoryConfigurationJaxImpl config :this.myDebRepositoryConfigManager.getConfigurationsForProject(project.getProjectId())) {
            	infoPack.add(new DebRepositoryConfigAndStatsWrapper(config, 
            										   myDebRepositoryManager.getRepositoryStatistics(
            												   						config, 
            												   						StringUtils.getDebRepoUrl(
            												   										myServer.getRootUrl(), 
            												   										config.getRepoName())
            												   						)
            										   )
            			);
            }
            if (!infoPack.isEmpty()) {
            	reposMap.put(project, infoPack);
            }
            project = project.getParentProject();
        }
        return reposMap;
	}

	@Override
	public boolean isAvailable(@NotNull HttpServletRequest request) {
		return super.isAvailable(request);
	}
	
	@Value @AllArgsConstructor
	public static class DebRepositoryConfigAndStatsWrapper {
		DebRepositoryConfigurationJaxImpl debRepositoryConfiguration;
		DebRepositoryStatistics debRepositoryStatistics;
	}

}