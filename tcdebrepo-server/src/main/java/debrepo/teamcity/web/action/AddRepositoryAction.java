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

import static debrepo.teamcity.web.DebRepoConfigurationEditPageActionController.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.service.DebRepositoryConfigurationFactory;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.util.RepositoryNameValidator;
import debrepo.teamcity.util.RepositoryNameValidator.RepositoryNameValidationResult;
import debrepo.teamcity.web.DebRepoConfigurationEditPageActionController;
import jetbrains.buildServer.controllers.ActionMessages;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.web.openapi.ControllerAction;

public class AddRepositoryAction extends ArtifactFilterAction implements ControllerAction {

	private final ProjectManager myProjectManager;
	private final DebRepositoryConfigurationManager myDebRepositoryConfigurationManager;
	private final DebRepositoryConfigurationFactory myDebRepositoryConfigurationFactory;
	private final static String ADD_REPO_ACTION = "addDebRepo";

	public AddRepositoryAction(@NotNull ProjectManager projectManager,
							   @NotNull final DebRepositoryConfigurationManager debRepositoryConfigurationManager,
							   @NotNull final DebRepositoryConfigurationFactory debRepositoryConfigurationFactory, 
							   @NotNull final DebRepoConfigurationEditPageActionController controller) {

		myProjectManager = projectManager;
		myDebRepositoryConfigurationManager = debRepositoryConfigurationManager;
		myDebRepositoryConfigurationFactory = debRepositoryConfigurationFactory;
		controller.registerAction(this);
	}
	
	@Override
	public String getFilterAction() {
		return ADD_REPO_ACTION;
	}

	public void process(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response,
			@Nullable final Element ajaxResponse) {

		String repoName;
		String projectId;
		try {
			repoName = getParameterAsStringOrNull(request, DEBREPO_NAME, "Please enter a Repository Name");
			projectId = getParameterAsStringOrNull(request, DEBREPO_PROJECT_ID, "Oops. Shouldn't happen");
		} catch (IncompleteFilterException e) {
			ajaxResponse.setAttribute("error", e.getMessage());
			return;
		}
		
		RepositoryNameValidationResult validationResult = new RepositoryNameValidator().nameIsURlSafe(repoName);
		if (validationResult.isError()){
			ajaxResponse.setAttribute("error", validationResult.getReason());
			return;
		}
		
		final SProject p = myProjectManager.findProjectByExternalId(projectId);
		if (p == null) {
			ajaxResponse.setAttribute("error", "A problem occurred locating the project");
			return;
		}
		final DebRepositoryConfiguration repoConfig = myDebRepositoryConfigurationFactory.createDebRepositoryConfiguration(p.getProjectId(), repoName);
        final DebRepositoryConfigurationManager.DebRepositoryActionResult result = myDebRepositoryConfigurationManager.addDebRepository(repoConfig);
 
		if (!result.isError()) {
			ActionMessages.getOrCreateMessages(request).addMessage("repoInfoUpdateResult",
				"Debian Repository '" + repoConfig.getRepoName() + "' successfully updated");
			ajaxResponse.setAttribute("status", "OK");
			ajaxResponse.setAttribute("redirect", "true");
		} else {
			ajaxResponse.setAttribute("error", result.getReason());
		}
	}


}