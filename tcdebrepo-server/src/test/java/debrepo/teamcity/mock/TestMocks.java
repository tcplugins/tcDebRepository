/*******************************************************************************
 * Copyright 2016, 2017 Net Wolf UK
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
package debrepo.teamcity.mock;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.plugins.classLoaders.TeamCityPluginClassLoader;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.web.openapi.PagePlace;
import jetbrains.buildServer.web.openapi.PagePlaces;
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
		WebControllerManager webControllerManager = mock(WebControllerManager.class);
		when(webControllerManager.getPlaceById(PlaceId.EDIT_PROJECT_PAGE_TAB)).thenReturn(pagePlace());
		when(webControllerManager.getPlaceById(PlaceId.BUILD_CONF_TAB)).thenReturn(pagePlace());
		when(webControllerManager.getPlaceById(PlaceId.ADMIN_SERVER_CONFIGURATION_TAB)).thenReturn(pagePlace());
		return webControllerManager;
	}
	
	@Bean
	AuthorizationInterceptor authorizationInterceptor() {
		return mock(AuthorizationInterceptor.class);
	}
	
	@Bean
	ProjectManager projectManager() {
		//when(webControllerManager().getPlaceById(PlaceId.BUILD_CONF_TAB)).thenReturn(pagePlace());
		return mock(ProjectManager.class);
	}
	
	@Bean
	ProjectSettingsManager projectSettingsManager() {
		return mock(ProjectSettingsManager.class);
	}

	@Bean
	ServerPaths serverPaths() {
		ServerPaths serverPaths = mock(ServerPaths.class);
		when(serverPaths.getPluginDataDirectory()).thenReturn(new File("target/pluginData"));
		return serverPaths;
	}
	
	@Bean
	PluginDescriptor pluginDescriptor() {
		PluginDescriptor pluginDescriptor = mock(PluginDescriptor.class);
		when(pluginDescriptor.getPluginName()).thenReturn("tcDebRepository");
		when(pluginDescriptor.getPluginResourcesPath(anyString())).thenReturn("something/debRepository/projectConfigTab.jsp");
		return pluginDescriptor;
	}
	
	@Bean
	SecurityContext securityContext() {
		return mock(SecurityContext.class);
	}
	
	@Bean 
	PagePlaces pagePlaces() {
		return webControllerManager();
	}
	
	@Bean 
	PagePlace pagePlace() {
		PagePlace pagePlace = mock(PagePlace.class);
		return pagePlace;
	}
	
	@Bean
	ClassLoader classLoader() {
		return Thread.currentThread().getContextClassLoader();
	}
	
}
