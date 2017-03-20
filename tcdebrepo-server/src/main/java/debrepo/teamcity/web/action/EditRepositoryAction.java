/*******************************************************************************
 *
 *  Copyright 2016 Net Wolf UK
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  
 *******************************************************************************/
package debrepo.teamcity.web.action;

import static debrepo.teamcity.web.DebRepoConfigurationEditPageActionController.DEBREPO_NAME;
import static debrepo.teamcity.web.DebRepoConfigurationEditPageActionController.DEBREPO_PROJECT_ID;
import static debrepo.teamcity.web.DebRepoConfigurationEditPageActionController.DEBREPO_RESTRICTED;
import static debrepo.teamcity.web.DebRepoConfigurationEditPageActionController.DEBREPO_UUID;

import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.util.RepositoryNameValidator;
import debrepo.teamcity.util.RepositoryNameValidator.RepositoryNameValidationResult;
import debrepo.teamcity.web.DebRepoConfigurationEditPageActionController;
import jetbrains.buildServer.controllers.ActionMessages;
import jetbrains.buildServer.web.openapi.ControllerAction;

public class EditRepositoryAction extends ArtifactFilterAction implements ControllerAction {

	private final DebRepositoryConfigurationManager myDebRepositoryConfigurationManager;
	private final static String EDIT_REPO_ACTION = "editDebRepo";

	public EditRepositoryAction(@NotNull final DebRepositoryConfigurationManager debRepositoryConfigurationManager,
									@NotNull final DebRepoConfigurationEditPageActionController controller) {

		myDebRepositoryConfigurationManager = debRepositoryConfigurationManager;
		controller.registerAction(this);
	}
	
	@Override
	public String getFilterAction() {
		return EDIT_REPO_ACTION;
	}

	public void process(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response,
			@Nullable final Element ajaxResponse) {

		String repoUuid;
		String repoName;
		String projectId;
		boolean restricted = false;
		Set<String> archs = new TreeSet<>();
		try {
			repoUuid = getParameterAsStringOrNull(request, DEBREPO_UUID, "request is missing repo uuid");
			repoName = getParameterAsStringOrNull(request, DEBREPO_NAME, "Please enter a Repository Name");
			projectId = getParameterAsStringOrNull(request, DEBREPO_PROJECT_ID, "Please choose a project");
			restricted = Boolean.valueOf(getParameterAsStringOrNull(request, DEBREPO_RESTRICTED, "Request is missing restricted setting"));
		} catch (IncompleteFilterException e) {
			ajaxResponse.setAttribute("error", e.getMessage());
			return;
		}
		
		Enumeration<String> attrs =  request.getParameterNames();
		while(attrs.hasMoreElements()) {
			String paramName = attrs.nextElement();
			if (paramName.startsWith("debrepo.arch.") && request.getParameter(paramName) != null){
				archs.add(request.getParameter(paramName).toString());
			}
		}
		
		
		RepositoryNameValidationResult validationResult = new RepositoryNameValidator().nameIsURlSafe(repoName);
		if (validationResult.isError()){
			ajaxResponse.setAttribute("error", validationResult.getReason());
			return;
		}
		
		DebRepositoryConfiguration debConfig = myDebRepositoryConfigurationManager.getDebRepositoryConfiguration(repoUuid);
		
		if (debConfig != null) {
			boolean change = false;
			boolean redirect = false;
			if (!debConfig.getRepoName().equals(repoName)) {
				debConfig.setRepoName(repoName);
				change = true;
				redirect= true;
			}
			if (!debConfig.getProjectId().equals(projectId)) {
				debConfig.setProjectId(projectId);
				change = true;
			}
			
			if (debConfig.isRestricted() != restricted) {
				debConfig.setRestricted(restricted);
				change = true;
			}
			
			if (!archs.equals(debConfig.getArchitecturesRepresentedByAll())) {
				debConfig.setArchitecturesRepresentedByAll(archs);
				change = true;
			}
			
			if (!change) {
				ActionMessages.getOrCreateMessages(request).addMessage("repoInfoUpdateResult",
						"No changes made to Debian Repository '" + debConfig.getRepoName());
				ajaxResponse.setAttribute("status", "OK");
				return;
			}
			
		    final DebRepositoryConfigurationManager.DebRepositoryActionResult result = myDebRepositoryConfigurationManager.editDebRepositoryConfiguration(debConfig);
 
			if (!result.isError()) {
				ActionMessages.getOrCreateMessages(request).addMessage("repoInfoUpdateResult",
						"Debian Repository '" + debConfig.getRepoName() + "' successfully updated");
				ajaxResponse.setAttribute("status", "OK");
				if (redirect) {
					ajaxResponse.setAttribute("redirect", "true");
				}
			} else {
				ajaxResponse.setAttribute("error", result.getReason());
			}
		} else {
			ajaxResponse.setAttribute("error", "The debian repository was not found and therefore, not edited.");
		}
	}


}