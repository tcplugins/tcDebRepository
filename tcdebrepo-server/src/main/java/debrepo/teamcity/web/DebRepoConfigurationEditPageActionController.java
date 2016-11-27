package debrepo.teamcity.web;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.controllers.BaseAjaxActionController;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

/**
 * This class simply holds the actions available at  "/admin/manageDebianRepository.html"
 * Actions need to inject this class and register themselves.
 */
public class DebRepoConfigurationEditPageActionController extends BaseAjaxActionController {
	
    public static final String DEBREPO_UUID = "debrepo.uuid";
    public static final String DEBREPO_NAME = "debrepo.name";

  public DebRepoConfigurationEditPageActionController(@NotNull final PluginDescriptor pluginDescriptor,
                                        	   @NotNull final WebControllerManager controllerManager) {
    super(controllerManager);
    controllerManager.registerController("/admin/debianRepositoryAction.html", this);
  }
    
}