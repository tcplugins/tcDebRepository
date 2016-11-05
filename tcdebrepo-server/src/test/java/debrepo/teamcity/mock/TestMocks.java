package debrepo.teamcity.mock;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

@Configuration
public class TestMocks {
	
	@Bean
	SBuildServer sbBuildServer() {
		return mock(SBuildServer.class);
	}
	
	@Bean
	WebControllerManager webControllerManager() {
		return mock(WebControllerManager.class);
	}
	
	@Bean
	AuthorizationInterceptor authorizationInterceptor() {
		return mock(AuthorizationInterceptor.class);
	}
	
	@Bean
	ProjectManager projectManager() {
		return mock(ProjectManager.class);
	}
	
	@Bean
	ProjectSettingsManager projectSettingsManager() {
		return mock(ProjectSettingsManager.class);
	}

	@Bean
	ServerPaths serverPaths() {
		return mock(ServerPaths.class);
	}
	
	@Bean
	PluginDescriptor pluginDescriptor() {
		PluginDescriptor pluginDescriptor = mock(PluginDescriptor.class);
		when(pluginDescriptor.getPluginResourcesPath(anyString())).thenReturn("something/debRepository/projectConfigTab.jsp");
		return pluginDescriptor;
	}
	
}
