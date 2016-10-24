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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.web.openapi.PagePlace;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;

public class DebRepoProjectSettingsPageTest {
	
	@Mock SBuildServer sBuildServer;
	@Mock ProjectSettingsManager settings;
	@Mock PluginDescriptor descriptor;
	@Mock PagePlaces pagePlaces;
	@Mock PagePlace pagePlace;
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		when(pagePlaces.getPlaceById(PlaceId.EDIT_PROJECT_PAGE_TAB)).thenReturn(pagePlace);
		when(descriptor.getPluginResourcesPath(anyString())).thenReturn("something/debRepository/projectConfigTab.jsp");
	}
	
	@Test
	public void testFillModelMapOfStringObjectHttpServletRequest() {
		DebRepoProjectSettingsPage page  = new DebRepoProjectSettingsPage(pagePlaces, descriptor, settings, sBuildServer);
		
		//fail("Not yet implemented");
	}

}
