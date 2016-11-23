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
            <forms:addButton id="createNewRepo" onclick="DebRepoPlugin.addRepo('${projectId}'); return false">Add new Repository</forms:addButton>
        </div>
    </c:if>

    <bs:refreshable containerId="DebRepos" pageUrl="${pageUrl}">
        <div class="sqsList">
            <c:choose>
                <c:when test="${fn:length(availableServersMap) > 0}">
                    <table class="sqsTable parametersTable">
                        <tr>
                            <th class="name">Name</th>
                            <c:if test="${userHasPermissionManagement}">
                                <th class="actions" colspan="2">Rename</th>
                            </c:if>
                        </tr>
                        <c:forEach items="${availableServersMap}" var="projectServersEntry">
                            <c:forEach items="${projectServersEntry.value}" var="server">
                                <tr class="sqsInfo">
                                    <td class="name"><c:out value="${server.name}"/>
                                        <c:if test="${projectServersEntry.key.externalId != projectId}"> belongs to
                                            <admin:editProjectLink projectId="${projectServersEntry.key.externalId}">
                                                <c:out value="${projectServersEntry.key.name}"/>
                                            </admin:editProjectLink>
                                        </c:if>
                                    </td>
                                    <td class="url">
                                        <div class="url"><c:out value="${server.url}"/></div>
                                        <c:if test="${userHasPermissionManagement && afn:permissionGrantedForProject(projectServersEntry.key, 'EDIT_PROJECT')}">
                                            <c:choose>
                                                <c:when test="${not empty server.login}">
                                                    <div class="login">Username: <c:out value="${server.login}"/></div>
                                                    <div class="password">Password: *****</div>
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="authentication grayNote">Anonymous</div>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:if>
                                    </td>
                                    <td class="db">
                                        <c:choose>
                                            <c:when test="${not empty server.JDBCUrl}"><div class="url"><c:out value="${server.JDBCUrl}"/></div></c:when>
                                            <c:otherwise><div class="defaultValue grayNote">jdbc:h2:tcp://localhost:9092/sonar</div></c:otherwise>
                                        </c:choose>
                                        <c:if test="${userHasPermissionManagement && afn:permissionGrantedForProject(projectServersEntry.key, 'EDIT_PROJECT')}">
                                            <c:choose>
                                                <c:when test="${not empty server.JDBCUsername}"><div class="dbUser">Username: <c:out value="${server.JDBCUsername}"/></div></c:when>
                                                <c:otherwise><div class="defaultValue grayNote">Username: sonar</div></c:otherwise>
                                            </c:choose>
                                            <c:choose>
                                                <c:when test="${not empty server.JDBCPassword}"><div class="dbPass">Password: *****</div></c:when>
                                                <c:otherwise><div class="defaultValue grayNote">Password: sonar</div></c:otherwise>
                                            </c:choose>
                                        </c:if>
                                    </td>
                                    <c:if test="${userHasPermissionManagement && afn:permissionGrantedForProject(projectServersEntry.key, 'EDIT_PROJECT')}">
                                        <td class="remove">
                                            <a id="removeNewServer" href="#"
                                            onclick="DebRepoPlugin.removeRepo('${projectServersEntry.key.externalId}', '${server.id}'); return false">remove...</a>
                                        </td>
                                        <td class="edit">
                                            <a id="editServer" href="#"
                                            onclick="DebRepoPlugin.editRepo({id: '${server.uuid}', name: '${server.name}',
                                                }', projectId: '${projectServersEntry.key.externalId}'}); return false">edit</a>
                                        </td>
                                    </c:if>
                                    <c:if test="${userHasPermissionManagement && not afn:permissionGrantedForProject(projectServersEntry.key, 'EDIT_PROJECT')}">
                                        <td colspan="2" class="grayNote"></td>
                                    </c:if>
                                </tr>
                            </c:forEach>
                        </c:forEach>
                    </table>
                </c:when>
                <c:otherwise>
                    <div class="noNoReposFound">
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
        <forms:multipartForm id="debRepoForm"
                             action="/admin/manageSonarServers.html"
                             targetIframe="hidden-iframe"
                             onsubmit="return DebRepoPlugin.RepoConfigurationDialog.doPost();">

            <table class="runnerFormTable">
                <tr>
                    <th>Name<l:star/></th>
                    <td>
                        <div><input type="text" id="serverinfo.name" name="serverinfo.name"/></div>
                    </td>
                </tr>
            </table>
            <input type="hidden" id="serverinfo.id" name="serverinfo.id"/>
            <input type="hidden" name="action" id="DebRepoaction" value="addRepo"/>
            <input type="hidden" name="projectId" id="projectId" value="${projectId}"/>
            <input type="hidden" name="publicKey" id="publicKey" value="${publicKey}"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="repoConfigurationDialogSubmit" label="Save"/>
                <forms:cancel onclick="DebRepoPlugin.RepoConfigurationDialog.close()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>

</div>
	
