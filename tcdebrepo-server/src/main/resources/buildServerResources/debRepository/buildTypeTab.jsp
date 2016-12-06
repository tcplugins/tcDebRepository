<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include-internal.jsp" %>


	        <div class="repoList">
	        <h2 class="noBorder">Debian Package Repositories</h2>
	            <c:choose>
	                <c:when test="${fn:length(repoConfigs) > 0}">
	                	<div class="noReposFound" style="padding:1.5em;">
	                		The following Debian Repositories are configured to filter artifacts published by this build.
	                    <table class="repoTable settings parametersTable" >
	                        <tr>
	                            <th class="name">Name</th>
	                            <th class="name" colspan="2">Actions</th>
	                        </tr>
	                        <c:forEach items="${repoConfigs}" var="repo">
	                                <tr class="repoInfo">
	                                    <td class="name" style="width:33%;">
	                                      <c:out value="${repo.repoName}"/>
	                                    </td>
	                                    <td class="edit">
                                            <a href="../app/debrepo/${repo.repoName}/">browse</a>
                                        </td>
	                                    <td class="edit">
                                            <a href="admin/editDebianRepository.html?repo=${repo.repoName}">edit</a>
                                        </td>
	                                </tr>
	                        </c:forEach>
	                    </table>
	                    </div>
	                </c:when>
	                <c:otherwise>
	                    <div class="noReposFound" style="padding:1.5em;">
	                        No Debian Repositories are watching this build. Artifacts from this build will not be being published in a repository.
	                    </div>
	                </c:otherwise>
	            </c:choose>
	        </div>