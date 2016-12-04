<%@ include file="/include.jsp" %>

<c:set var="pageTitle" value="Debian Repositories" scope="request"/>

<bs:page>
  <jsp:attribute name="head_include">
    <bs:linkCSS>
      /css/admin/adminMain.css
      ${teamcityPluginResourcesPath}debRepository/css/debRepository.css
    </bs:linkCSS>
    <bs:linkScript>
      ${teamcityPluginResourcesPath}debRepository/editDebianRepository.js
    </bs:linkScript>
    <script type="text/javascript">
      BS.Navigation.items = [
        {title: "Administration", url: '<c:url value="/admin/admin.html"/>'},
        {title: "Debian Repositories", selected: true}
      ];
    </script>
  </jsp:attribute>

  <jsp:attribute name="body_include">
  
	<div class="manageRepos">
	    <h2 class="noBorder">Debian Package Repositories</h2>
	    		<div class="grayNote">
				A Debian Package Repository provides the apt tools (apt-get, aptitude, synatpic) on Debian computers (or Ubuntu and other derivative distros) a location to locate and download software packages.<br>
				Creating a repository allows TeamCity to act as a Debian Package Repository serving the .deb files produced by your builds along with the meta-data required for the apt tools to locate packages.
				</div>
	
	<bs:messages key="repoUpdateResult"/>
	
	        <div class="repoList">
	            <c:choose>
	                <c:when test="${fn:length(repoConfigs) > 0}">
	                    <table class="repoTable settings">
	                        <tr>
	                            <th class="name">Name</th>
	                            <th class="name" colspan="2">Actions</th>
	                            <th class="name">Information</th>
	                        </tr>
	                        <c:forEach items="${repoConfigs}" var="repo">
	                                <tr class="repoInfo">
	                                    <td class="name" rowspan=3 style="width:33%;">
	                                    <a href="../../app/debrepo/${repo.debRepositoryConfiguration.repoName}/">
	                                      <c:out value="${repo.debRepositoryConfiguration.repoName}"/>
	                                    </a>
	                                         belongs to
	                                         <c:choose>
	                                         	<c:when test="${repo.permissionedOnProject}">
	                                         	<a href="editProject.html?projectId=${repo.project.externalId}&tab=tcdebrepo"><c:out value="${repo.project.name}"/></a>
	                                         	</c:when>
	                                         	<c:otherwise>
	                                                <c:out value="${repo.project.name}"/>
	                                            </c:otherwise>
	                                          </c:choose>
	                                    </td>
	                                    <td class="edit" rowspan=3>
		                                            <a href="../../app/debrepo/${repo.debRepositoryConfiguration.repoName}/">browse</a>
                                        </td>
	                                    <td class="edit" rowspan=3>
		                                    <c:if test="${repo.permissionedOnProject}">
		                                            <a href="editDebianRepository.html?repo=${repo.debRepositoryConfiguration.repoName}">edit</a>
		                                    </c:if>
		                                    <c:if test="not ${repo.permissionedOnProject}">
		                                            <span class="grayNote" title="You are not permissioned to edit this project">edit</span>
		                                    </c:if>
                                        </td>
	                                    <td style="width:33%;">
	                                    	Builds Types: ${fn:length(repo.debRepositoryConfiguration.buildTypes)}
	                                    </td>
	                                    </td>
	                                </tr>
	                                <tr><td style="width:33%;">Package Listings: ${repo.debRepositoryStatistics.totalPackageCount}</td></tr>
	                                <tr><td style="width:33%;">Artifact Filters: ${repo.debRepositoryStatistics.totalFilterCount}</td></tr>
	                        </c:forEach>
	                    </table>
	                </c:when>
	                <c:otherwise>
	                    <div class="noReposFound">
	                        No Debian Repositories have been created yet. To create a repository, visit the "Debian Repositories" tab whilst editing a project.
	                    </div>
	                </c:otherwise>
	            </c:choose>
	        </div>
	
	</div>
</jsp:attribute>
</bs:page>
	