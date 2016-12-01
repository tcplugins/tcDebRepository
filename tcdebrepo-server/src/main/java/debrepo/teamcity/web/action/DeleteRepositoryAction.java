package debrepo.teamcity.web.action;

import static debrepo.teamcity.web.DebRepoConfigurationEditPageActionController.DEBREPO_UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.web.DebRepoConfigurationEditPageActionController;
import jetbrains.buildServer.controllers.ActionMessages;
import jetbrains.buildServer.web.openapi.ControllerAction;

public class DeleteRepositoryAction extends ArtifactFilterAction implements ControllerAction {

	private final DebRepositoryConfigurationManager myDebRepositoryConfigurationManager;
	private final static String DELETE_REPO_ACTION = "deleteDebRepo";

	public DeleteRepositoryAction(@NotNull final DebRepositoryConfigurationManager debRepositoryConfigurationManager,
									@NotNull final DebRepoConfigurationEditPageActionController controller) {

		myDebRepositoryConfigurationManager = debRepositoryConfigurationManager;
		controller.registerAction(this);
	}
	
	@Override
	public String getFilterAction() {
		return DELETE_REPO_ACTION;
	}

	public void process(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response,
			@Nullable final Element ajaxResponse) {
		String repoUuid = request.getParameter(DEBREPO_UUID);
		DebRepositoryConfiguration debConfig = myDebRepositoryConfigurationManager
				.getDebRepositoryConfiguration(repoUuid);

		if (debConfig != null) {
            final DebRepositoryConfigurationManager.DebRepositoryActionResult result = myDebRepositoryConfigurationManager.removeDebRespository(debConfig);
 
			if (!result.isError()) {
				ActionMessages.getOrCreateMessages(request).addMessage("repoUpdateResult",
						"Debian Repository '" + debConfig.getRepoName() + "' successfully deleted");
				ajaxResponse.setAttribute("status", "OK");
			} else {
				ajaxResponse.setAttribute("error", result.getReason());
			}
		} else {
			ajaxResponse.setAttribute("error", "The debian repository was not found and therefore, not deleted.");
		}
	}

}