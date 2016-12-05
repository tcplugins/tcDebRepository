<%@ page import="jetbrains.buildServer.serverSide.crypt.RSACipher" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include-internal.jsp" %>
<%--@elvariable id="availableServersMap" type="java.util.Map<jetbrains.buildServer.serverSide.SProject, java.util.List<jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSInfo>"--%>
<%--@elvariable id="projectId" type="java.lang.String"--%>
<%--@elvariable id="userHasPermissionManagement" type="java.lang.Boolean"--%>

<div class="manageRepos">
    <h2 class="noBorder">Debian Package Repositories</h2>
    		<div class="grayNote">
			A Debian Package Repository provides Debian's apt on Debian (or Ubuntu and other derivative distros) computers to locate and download software packages.<br>
			Creating a repository allows TeamCity to act as a Debian Package Repository serving the .deb files produced by your builds.
			</div>

    <c:if test="${userHasPermissionManagement}">
        <div class="add">
            <forms:addButton id="createNewRepo" onclick="DebRepoPlugin.addDebRepo('${projectId}'); return false">Add new Repository</forms:addButton>
        </div>
    </c:if>

    <bs:refreshable containerId="DebRepos" pageUrl="${pageUrl}">
        <div class="repoList">
            <c:choose>
                <c:when test="${fn:length(repositoriesMap) > 0}">
                    <table class="repoTable parametersTable">
                        <tr>
                            <th class="name">Name</th>
                            <th class="name">Information</th>
                            <c:if test="${userHasPermissionManagement}">
                                <th class="actions" colspan="2">Actions</th>
                            </c:if>
                        </tr>
                        <c:forEach items="${repositoriesMap}" var="projectReposEntry">
                            <c:forEach items="${projectReposEntry.value}" var="repo">
                                <tr class="repoInfo">
                                    <td class="name" rowspan=3><c:out value="${repo.debRepositoryConfiguration.repoName}"/>
                                        <c:if test="${projectReposEntry.key.externalId != projectId}"> belongs to
                                            <admin:editProjectLink projectId="${projectReposEntry.key.externalId}">
                                                <c:out value="${projectReposEntry.key.name}"/>
                                            </admin:editProjectLink>
                                        </c:if>
                                    </td>
                                    <td>
                                    	Builds Types: ${fn:length(repo.debRepositoryConfiguration.buildTypes)}
                                    </td>
                                    </td>
                                    <c:if test="${userHasPermissionManagement && afn:permissionGrantedForProject(projectReposEntry.key, 'EDIT_PROJECT')}">
                                        <td class="edit" rowspan=3>
                                            <a id="editServer" href="editDebianRepository.html?repo=${repo.debRepositoryConfiguration.repoName}">edit</a>
                                        </td>
                                    </c:if>
                                    <c:if test="${userHasPermissionManagement && not afn:permissionGrantedForProject(projectReposEntry.key, 'EDIT_PROJECT')}">
                                        <td rowspan=3 class="grayNote edit"><span title="PROJECT_EDIT permission required">edit</span></td>
                                        
                                    </c:if>
                                </tr>
                                <tr><td>Artifact Filters: ${repo.debRepositoryStatistics.totalFilterCount}</td></tr>
                                <tr><td>Package Listings: ${repo.debRepositoryStatistics.totalPackageCount}</td></tr>
                            </c:forEach>
                        </c:forEach>
                    </table>
                </c:when>
                <c:otherwise>
                    <div class="noReposFound">
                        No Debian Repositories have been created yet.
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </bs:refreshable>

    <bs:dialog dialogId="repoConfigDialog"
               dialogClass="repoConfigDialog"
               title="Edit Debian Repository"
               closeCommand="DebRepoPlugin.RepoConfigurationDialog.close()">
        <forms:multipartForm id="repoConfigForm"
                             action="/admin/tcDebRepository/manageDebianRepositories.html"
                             targetIframe="hidden-iframe"
                             onsubmit="return DebRepoPlugin.RepoConfigurationDialog.doPost();">

            <table class="runnerFormTable">
                <tr>
                    <th>Name<l:star/></th>
                    <td>
                        <div><input type="text" id="debrepo.name" name="debrepo.name"/></div>
                        <div id="ajaxResult"></div>
                    </td>
                </tr>
            </table>
            <input type="hidden" id="debrepo.uuid" name="debrepo.uuid"/>
            <input type="hidden" name="action" id="DebRepoaction" value="addDebRepo"/>
            <input type="hidden" name="projectId" id="projectId" value="${projectId}"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="repoConfigurationDialogSubmit" label="Save"/>
                <forms:cancel onclick="DebRepoPlugin.RepoConfigurationDialog.close()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>

</div>
	
