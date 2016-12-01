package debrepo.teamcity.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

public class DebRepoConfigurationEditPageController extends BaseController {

	private final PluginDescriptor myPluginDescriptor;
	private final DebRepositoryConfigurationManager myDebRepositoryConfigurationManager;
	private final SBuildServer myBuildServer;

	public DebRepoConfigurationEditPageController(@NotNull final SBuildServer buildServer,
			@NotNull final PluginDescriptor pluginDescriptor, @NotNull final WebControllerManager manager,
			@NotNull final DebRepositoryConfigurationManager debRepositoryConfigurationManager) {
		super(buildServer);
		myBuildServer = buildServer;
		myPluginDescriptor = pluginDescriptor;
		myDebRepositoryConfigurationManager = debRepositoryConfigurationManager;
		manager.registerController("/admin/editDebianRepository.html", this);
	}

	@Override
	protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) {
		DebRepositoryConfiguration repoConfig = getDebRepoFromRequest(request);
		if (repoConfig == null) {
			getOrCreateMessages(request).addMessage("debianRepositoryNotFound",
					"Selected Debian Repository no longer exists");
			return new ModelAndView(new RedirectView("/admin/debianRepositories.html", true));
		}
		ModelAndView mv = new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/editDebianRepository.jsp"));
		SProject myProject = myBuildServer.getProjectManager().findProjectById(repoConfig.getProjectId());
		EditDebRepositoryBean bean = EditDebRepositoryBean.build(myBuildServer.getProjectManager(), repoConfig, myProject);
		bean.getCameFromSupport().setUrlFromRequest(request, "/admin/editProject.html?projectId=" + myProject.getExternalId() + "&tab=tcdebrepo");
		bean.getCameFromSupport().setTitleFromRequest(request, "Priority Classes");
		mv.getModel().put("repoConfig", repoConfig);
		mv.getModel().put("debRepoBean", bean);
		mv.getModel().put("sortedProjectBuildTypes", myProject.getBuildTypes());

		return mv;
	}

	@Nullable
	private DebRepositoryConfiguration getDebRepoFromRequest(final HttpServletRequest request) {
		String debRepoName = request.getParameter("repo");
		if (debRepoName != null) {
			return myDebRepositoryConfigurationManager.getDebRepositoryConfigurationByName(debRepoName);
		} else {
			return null;
		}
	}

}
