package debrepo.teamcity.web.action;

import static debrepo.teamcity.web.DebRepoConfigurationEditPageActionController.*;

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
import debrepo.teamcity.web.action.ArtifactFilterAction.IncompleteFilterException;
import jetbrains.buildServer.controllers.ActionMessages;
import jetbrains.buildServer.web.openapi.ControllerAction;

public class EditArtifactFilterAction extends ArtifactFilterAction implements ControllerAction {

	private final DebRepositoryConfigurationManager myDebRepositoryConfigurationManager;
	private final static String EDIT_FILTER_ACTION = "editArtifactFilter";

	public EditArtifactFilterAction(@NotNull final DebRepositoryConfigurationManager debRepositoryConfigurationManager,
									@NotNull final DebRepoConfigurationEditPageActionController controller) {

		myDebRepositoryConfigurationManager = debRepositoryConfigurationManager;
		controller.registerAction(this);
	}
	
	@Override
	public String getFilterAction() {
		return EDIT_FILTER_ACTION;
	}

	public void process(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response,
			@Nullable final Element ajaxResponse) {
		String repoUuid = request.getParameter(DEBREPO_UUID);
		DebRepositoryConfiguration debConfig = myDebRepositoryConfigurationManager
				.getDebRepositoryConfiguration(repoUuid);
		Filter filter;
		String buildTypeId ;
		try {
			filter = getFilterFromRequest(request);
			buildTypeId = getParameterAsStringOrNull(request, DEBREPO_FILTER_BUILD_TYPE_ID, "Please select a Build Type");
		} catch (IncompleteFilterException e) {
			ajaxResponse.setAttribute("error", e.getMessage());
			return;
		}

		if (debConfig != null && buildTypeId != null && filter != null) {
			if (debConfig.containsBuildType(buildTypeId)) {
				for (DebRepositoryBuildTypeConfig config : debConfig.getBuildTypes()) {
					for (Filter f : config.getDebFilters()) {
						if (filter.getId().equals(f.getId())) {
							config.removeFilter(filter.getId());
							break;
						}
					}
				}
				for (DebRepositoryBuildTypeConfig config : debConfig.getBuildTypes()) {
					if (buildTypeId.equals(config.getBuildTypeId())) {
						config.addFilter(filter);
					}
				}
			} else {
				debConfig.addBuildType(new DebRepositoryBuildTypeConfig(buildTypeId).af(filter));
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
						"Artfact Filter updated");
				ajaxResponse.setAttribute("status", "OK");
			} else {
				ajaxResponse.setAttribute("error", result.getReason());
			}
		}
	}

}