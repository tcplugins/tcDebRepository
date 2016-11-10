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
package debrepo.teamcity.mock;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
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
	
	@Bean 
	PagePlaces pagePlaces() {
		PagePlaces pagePlaces = mock(PagePlaces.class);
		when(pagePlaces.getPlaceById(PlaceId.EDIT_PROJECT_PAGE_TAB)).thenReturn(pagePlace());
		return pagePlaces;
	}
	
	@Bean 
	PagePlace pagePlace() {
		return mock(PagePlace.class);
	}
}
