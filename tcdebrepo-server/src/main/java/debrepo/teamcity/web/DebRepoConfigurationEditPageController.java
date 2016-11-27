package debrepo.teamcity.web;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.jdbc.support.incrementer.MySQLMaxValueIncrementer;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import jetbrains.buildServer.controllers.BaseFormXmlController;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

public class DebRepoConfigurationEditPageController extends BaseFormXmlController {

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
	protected ModelAndView doGet(HttpServletRequest request, HttpServletResponse response) {
		DebRepositoryConfiguration repoConfig = getDebRepoFromRequest(request);
		if (repoConfig == null) {
			getOrCreateMessages(request).addMessage("debianRepositoryNotFound",
					"Selected Debian Repository no longer exists");
			return new ModelAndView(new RedirectView("/", true));
		}
		ModelAndView mv = new ModelAndView(myPluginDescriptor.getPluginResourcesPath("debRepository/editDebianRepository.jsp"));
		SProject myProject = myBuildServer.getProjectManager().findProjectById(repoConfig.getProjectId());
		EditDebRepositoryBean bean = EditDebRepositoryBean.build(myBuildServer.getProjectManager(), repoConfig, myProject);
		bean.getCameFromSupport().setUrlFromRequest(request, "/admin/editProject.html?projectId=" + myProject.getExternalId() + "&tab=tcdebrepo");
		bean.getCameFromSupport().setTitleFromRequest(request, "Priority Classes");
		mv.getModel().put("repoConfig", repoConfig);
		//List<SBuildType> sortedBuildTypes = repoConfig.getBuildTypes();
		mv.getModel().put("debRepoBean", bean);
		mv.getModel().put("sortedProjectBuildTypes", myProject.getBuildTypes());
		//Collections.sort(sortedBuildTypes);
		//mv.getModel().put("sortedBuildTypes", sortedBuildTypes);

		return mv;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response, Element xmlResponse) {
		// TODO Auto-generated method stub

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
