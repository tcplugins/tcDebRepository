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
 * 
 * 
 * Code in this class is based on the UI classes in the 
 * TeamCity.SonarQubePlugin by Andrey Titov (@linfar on Github)
 *
 * 
 *******************************************************************************/


package debrepo.teamcity.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.service.DebRepositoryConfigurationFactory;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryManager;
import jetbrains.buildServer.controllers.BaseAjaxActionController;
import jetbrains.buildServer.serverSide.ConfigAction;
import jetbrains.buildServer.serverSide.ConfigActionFactory;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.AuthUtil;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.ControllerAction;
import jetbrains.buildServer.web.openapi.WebControllerManager;

/**
 * Controller class to receive Ajax events from the {@link DebRepoProjectSettingsTab}
 * Manages the configuration of a project's DebRepositories.
 */
public class DebRepoProjectSettingsTabActionController extends BaseAjaxActionController implements ControllerAction {
    private static final String DEBREPO_UUID = "debrepo.uuid";
    private static final String DEBREPO_NAME = "debrepo.name";

    private static final String ADD_DEBREPO_ACTION = "addDebRepo";
    private static final String REMOVE_DEBREPO_ACTION = "removeDebRepo";
    private static final String EDIT_DEBREPO_ACTION = "editDebRepo";
    private static final String DEBREPO_ACTION = "action";
    @NotNull
    private final DebRepositoryManager myDebRepositoryManager;
    @NotNull
    private final DebRepositoryConfigurationManager myDebRepositoryConfigurationManager;
    @NotNull
    private final DebRepositoryConfigurationFactory myDebRepositoryConfigurationFactory;
    @NotNull
    private final ProjectManager myProjectManager;
    @NotNull
    private final SecurityContext mySecurityContext;
    @NotNull
    private final ConfigActionFactory myConfigActionFactory;

    public DebRepoProjectSettingsTabActionController(@NotNull final WebControllerManager controllerManager,
                                     @NotNull final DebRepositoryManager debRepositoryManager,
                                     @NotNull final DebRepositoryConfigurationManager debRepositoryConfigurationManager,
                                     @NotNull final DebRepositoryConfigurationFactory debRepositoryConfigurationFactory,
                                     @NotNull final ProjectManager projectManager,
                                     @NotNull final SecurityContext securityContext,
                                     @NotNull final ConfigActionFactory configActionFactory) {
        super(controllerManager);
        myConfigActionFactory = configActionFactory;
        controllerManager.registerController("/admin/tcDebRepository/manageDebianRepositories.html", this);
        registerAction(this);

        myDebRepositoryManager = debRepositoryManager;
        myDebRepositoryConfigurationManager = debRepositoryConfigurationManager;
        myDebRepositoryConfigurationFactory = debRepositoryConfigurationFactory;
        myProjectManager = projectManager;
        mySecurityContext = securityContext;
    }

    public boolean canProcess(@NotNull final HttpServletRequest request) {
        final String action = getAction(request);
        return ADD_DEBREPO_ACTION.equals(action) ||
               EDIT_DEBREPO_ACTION.equals(action) ||
               REMOVE_DEBREPO_ACTION.equals(action);
    }

    private static String getAction(@NotNull final HttpServletRequest request) {
        return request.getParameter(DEBREPO_ACTION);
    }

    public void process(@NotNull final HttpServletRequest request,
                        @NotNull final HttpServletResponse response,
                        @Nullable final Element ajaxResponse) {
        final SProject project = getProject(request);
        if (ajaxResponse == null || project == null) {
            return;
        }

        // Security test (user without management permission could access this controller)
        if (!AuthUtil.hasPermissionToManageProject(mySecurityContext.getAuthorityHolder(), project.getProjectId())){
            ajaxResponse.setAttribute("error", "User does not have permission to manage repositories for this project");
            return;
        }

        final String action = getAction(request);
        try {
            ConfigAction configAction = null;
            if (ADD_DEBREPO_ACTION.equals(action)) {
                final DebRepositoryConfiguration debRepoInfo = addDebRepoConfig(request, project, ajaxResponse);
                if (debRepoInfo != null) {
                    configAction = myConfigActionFactory.createAction(project, "Debian Repository '" + debRepoInfo.getRepoName() + "' has been created");
                }
            } else if (REMOVE_DEBREPO_ACTION.equals(action)) {
                final DebRepositoryConfiguration debRepoInfo = removeDebRepositoryConfiguration(request, project, ajaxResponse);
                if (debRepoInfo != null) {
                    configAction = myConfigActionFactory.createAction(project, "Debian Repository '" + debRepoInfo.getRepoName() + "' was removed");
                }
            } else if (EDIT_DEBREPO_ACTION.equals(action)) {
                final DebRepositoryConfiguration debRepoInfo = editDebRepoConfig(request, project, ajaxResponse);
                if (debRepoInfo != null) {
                    configAction = myConfigActionFactory.createAction(project, "Debian Repository settings '" + debRepoInfo.getRepoName() + "' were changed");
                }
            }
            if (configAction != null) {
                project.persist(configAction);
            }
        } catch (IOException e) {
            ajaxResponse.setAttribute("error", "Exception occurred: " + e.getMessage());
        }
    }

    private DebRepositoryConfiguration editDebRepoConfig(@NotNull final HttpServletRequest request,
                                @NotNull final SProject project,
                                @NotNull final Element ajaxResponse) {
        if (!validate(request, ajaxResponse)) {
            return null;
        }

        final String debRepoUuid = getRepoUUID(request);
        if (debRepoUuid == null) {
            ajaxResponse.setAttribute("error", "ID is not set");
            return null;
        }

        final DebRepositoryConfiguration old = myDebRepositoryConfigurationManager.getDebRepositoryConfiguration(debRepoUuid);
        if (old == null) {
            return null;
        }

        final DebRepositoryConfiguration debRepoConfig = buildDebRepoConfigFromRequest(request, project);
        final DebRepositoryConfigurationManager.DebRepositoryActionResult result = myDebRepositoryConfigurationManager.editDebRepositoryConfiguration(debRepoConfig);
        if (!result.isError()) {
            ajaxResponse.setAttribute("status", "OK");
        } else {
            ajaxResponse.setAttribute("error", result.getReason());
        }
        return result.getAfterAction();
    }


    @NotNull
    private DebRepositoryConfiguration buildDebRepoConfigFromRequest(@NotNull HttpServletRequest request, SProject project) {
    	DebRepositoryConfiguration existing;
    	
    	if (StringUtil.nullIfEmpty(request.getParameter(DEBREPO_UUID)) == null){
    		existing = myDebRepositoryConfigurationFactory.createDebRepositoryConfiguration(project.getProjectId(),
        			StringUtil.nullIfEmpty(request.getParameter(DEBREPO_NAME)));
    	} else {
    		existing = myDebRepositoryConfigurationFactory.copyDebRepositoryConfiguration(myDebRepositoryConfigurationManager, StringUtil.nullIfEmpty(request.getParameter(DEBREPO_UUID))); 
    	}
    	
        if (StringUtil.nullIfEmpty(request.getParameter(DEBREPO_NAME)) != null){
        	existing.setRepoName(request.getParameter(DEBREPO_NAME));
        }
        return existing;
    	
    }

    private DebRepositoryConfiguration removeDebRepositoryConfiguration(@NotNull final HttpServletRequest request,
                                  @NotNull final SProject project,
                                  @NotNull final Element ajaxResponse) throws IOException {
        final String repoUuid = getRepoUUID(request);
        if (repoUuid == null) {
            ajaxResponse.setAttribute("error", "ID is not set");
        } else {
        	final DebRepositoryConfiguration debRepoConfig = buildDebRepoConfigFromRequest(request, project);
            final DebRepositoryConfigurationManager.DebRepositoryActionResult result = myDebRepositoryConfigurationManager.removeDebRespository(debRepoConfig);
            if (!result.isError()) {
                ajaxResponse.setAttribute("status", result.getReason());
                return result.getBeforeAction();
            } else {
                ajaxResponse.setAttribute("error", result.getReason());
            }
    }
        return null;
    }

    private DebRepositoryConfiguration addDebRepoConfig(@NotNull final HttpServletRequest request,
                                  @NotNull final SProject project,
                                  @NotNull final Element ajaxResponse) throws IOException {
        if (validate(request, ajaxResponse)) {
            final DebRepositoryConfiguration repoConfig = buildDebRepoConfigFromRequest(request, project);
            final DebRepositoryConfigurationManager.DebRepositoryActionResult result = myDebRepositoryConfigurationManager.addDebRepository(repoConfig);
            if (!result.isError()) {
                ajaxResponse.setAttribute("status", "OK");
            } else {
                ajaxResponse.setAttribute("error", result.getReason());
            }
            return repoConfig;
        }
        return null;
    }

    private boolean validate(HttpServletRequest request, Element ajaxResponse) {
        if (request.getParameter(DEBREPO_NAME) == null) {
            ajaxResponse.setAttribute("error", "Respository name must be set");
            return false;
        }
        return true;
    }

    @Nullable
    private SProject getProject(@NotNull final HttpServletRequest request) {
        return myProjectManager.findProjectByExternalId(request.getParameter("projectId"));
    }

    private static String getRepoUUID(@NotNull final HttpServletRequest request) {
        return request.getParameter(DEBREPO_UUID);
    }

}
