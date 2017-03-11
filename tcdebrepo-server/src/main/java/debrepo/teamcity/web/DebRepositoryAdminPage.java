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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import debrepo.teamcity.ebean.DebPackageModel;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryMaintenanceManager;
import jetbrains.buildServer.controllers.admin.AdminPage;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;

public class DebRepositoryAdminPage extends AdminPage {
	final private DebRepositoryConfigurationManager myConfigurationManager;
	final private DebRepositoryMaintenanceManager myMaintenanceManager;

	public DebRepositoryAdminPage(@NotNull PagePlaces pagePlaces, 
								  @NotNull PluginDescriptor descriptor,
								  @NotNull DebRepositoryConfigurationManager configurationManager,
								  @NotNull DebRepositoryMaintenanceManager maintenanceManager
								  ) {
		super(pagePlaces);
		myConfigurationManager = configurationManager;
		myMaintenanceManager = maintenanceManager;
		setPluginName("tcDebRepository");
		setIncludeUrl(descriptor.getPluginResourcesPath("debRepository/adminTab.jsp"));
		setTabTitle("Debian Repositories");
		setPosition(PositionConstraint.after("clouds", "email", "jabber"));
		register();
	}

	@Override
	public boolean isAvailable(@NotNull HttpServletRequest request) {
		return super.isAvailable(request) && checkHasGlobalPermission(request, Permission.CHANGE_SERVER_SETTINGS);
	}

	@NotNull
	public String getGroup() {
		return SERVER_RELATED_GROUP;
	}
	
	@Override
	public void fillModel(Map<String, Object> model, HttpServletRequest request) {
		model.put("repoCount", myConfigurationManager.getAllConfigurations().size());
		model.put("totalPackageCount", myMaintenanceManager.getTotalPackageCount());
		model.put("totalFileCount", myMaintenanceManager.getTotalFileCount());
		model.put("filesAssociated", myMaintenanceManager.getAssociatedFileCount());
		model.put("filesUnassociated", myMaintenanceManager.getDanglingFileCount());
	}

}