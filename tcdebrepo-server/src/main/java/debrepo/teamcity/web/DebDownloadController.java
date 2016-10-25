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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import debrepo.teamcity.ebean.Customer;
import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.WebControllerManager;

public class DebDownloadController extends BaseController {
	
	public static final String DEBREPO_BASE_URL = "/app/debrepo";
	private static final String DEBREPO_BASE_URL_WITH_WILDCARD = DEBREPO_BASE_URL + "/**";

	public DebDownloadController(SBuildServer sBuildServer, WebControllerManager webControllerManager, AuthorizationInterceptor authorizationInterceptor) {
		super(sBuildServer);
		webControllerManager.registerController(DEBREPO_BASE_URL_WITH_WILDCARD, this);
		authorizationInterceptor.addPathNotRequiringAuth(DEBREPO_BASE_URL_WITH_WILDCARD);
	}
		
	@Override
	protected ModelAndView doHandle(HttpServletRequest arg0, HttpServletResponse arg1) throws Exception {
		Customer c = new Customer();
		c.setName("netwolfuk");
		c.save();
		return simpleView("Weeeeee. We found " + Customer.find.findCount() + " rows.");
	}

}
