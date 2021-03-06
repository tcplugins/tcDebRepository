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

import static debrepo.teamcity.web.DebRepoConfigurationEditPageActionController.DEBREPO_FILTER_BUILD_TYPE_ID;
import static debrepo.teamcity.web.DebRepoConfigurationEditPageActionController.DEBREPO_FILTER_ID;
import static debrepo.teamcity.web.DebRepoConfigurationEditPageActionController.DEBREPO_UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig.Filter;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.web.DebRepoConfigurationEditPageActionController;
import jetbrains.buildServer.controllers.ActionMessages;
import jetbrains.buildServer.web.openapi.ControllerAction;

public class DeleteArtifactFilterAction extends ArtifactFilterAction implements ControllerAction {

	private final DebRepositoryConfigurationManager myDebRepositoryConfigurationManager;
	private final static String DELETE_FILTER_ACTION = "deleteArtifactFilter";

	public DeleteArtifactFilterAction(@NotNull final DebRepositoryConfigurationManager debRepositoryConfigurationManager,
									@NotNull final DebRepoConfigurationEditPageActionController controller) {

		myDebRepositoryConfigurationManager = debRepositoryConfigurationManager;
		controller.registerAction(this);
	}
	
	@Override
	public String getFilterAction() {
		return DELETE_FILTER_ACTION;
	}

	public void process(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response,
			@Nullable final Element ajaxResponse) {
		String repoUuid = request.getParameter(DEBREPO_UUID);
		DebRepositoryConfiguration debConfig = myDebRepositoryConfigurationManager
				.getDebRepositoryConfiguration(repoUuid);
		String buildTypeId ;
		String filterId;
		try {
			filterId = getParameterAsStringOrNull(request, DEBREPO_FILTER_ID, "missing debrepo.filter.id. oops, you shouldn't see this");
			buildTypeId = getParameterAsStringOrNull(request, DEBREPO_FILTER_BUILD_TYPE_ID, "missing debrepo.filter.buildtypeid. oops, you shouldn't see this");
		} catch (IncompleteFilterException e) {
			ajaxResponse.setAttribute("error", e.getMessage());
			return;
		}

		if (debConfig != null && buildTypeId != null && filterId != null) {
			if (debConfig.containsBuildType(buildTypeId)) {
				for (DebRepositoryBuildTypeConfig config : debConfig.getBuildTypes()) {
					for (Filter f : config.getDebFilters()) {
						if (filterId.equals(f.getId())) {
							config.removeFilter(filterId);
							break;
						}
					}
				}
			} else {
				ajaxResponse.setAttribute("error", "Failed to delete. This build does not appear to be configured in this repository.");
			}
			
			for (DebRepositoryBuildTypeConfig config : debConfig.getBuildTypes()) {
				if (config.getDebFilters().isEmpty()) {
					debConfig.getBuildTypes().remove(config);
				}
			}

			DebRepositoryConfigurationManager.DebRepositoryActionResult result = myDebRepositoryConfigurationManager
					.editDebRepositoryConfiguration(debConfig);

			if (!result.isError()) {
				ActionMessages.getOrCreateMessages(request).addMessage("filterUpdateResult",
						"Artfact Filter deleted");
				ajaxResponse.setAttribute("status", "OK");
			} else {
				ActionMessages.getOrCreateMessages(request).addMessage("filterUpdateResult",
						"Sorry, the Artifact Filter could not be updated.");
				ajaxResponse.setAttribute("error", result.getReason());
			}
		}
	}

}