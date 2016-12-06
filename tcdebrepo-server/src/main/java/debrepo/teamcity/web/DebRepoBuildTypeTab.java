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
