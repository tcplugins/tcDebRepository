<%--
~ Copyright 2000-2014 JetBrains s.r.o.
~
~ Licensed under the Apache License, Version 2.0 (the "License");
~ you may not use this file except in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing, software
~ distributed under the License is distributed on an "AS IS" BASIS,
~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~ See the License for the specific language governing permissions and
~ limitations under the License.
--%>
<%@ include file="/include.jsp" %>

<c:set var="pageTitle" value="Edit Debian Repository ${repoConfig.repoName}" scope="request"/>

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
        {title: "Build Queue", url: '<c:url value="/queue.html"/>'},
        {title: "Priority Classes", url: '<c:url value="${teamcityPluginResourcesPath}priorityClassList.html"/>'},
        {title: '<c:out value="${debRepoBean.name}"/>', selected: true}
      ];
    </script>
  </jsp:attribute>

  <jsp:attribute name="body_include">
  	<h2>Debian Repository : ${repoConfig.repoName}</h2>

      <table>
<!-- <tr><td colspan=2><p>The repository name forms part of the URL for accessing this repository. Names MUST be unique across a TeamCity 
                    instance and must only contain URL compatible characters.</p> 
                <p>Renaming a repository will require all Debian servers 
                    with the URL in their sources.list to be updated. You should not to rename a repository very often.</p></td> 
        </tr>  -->
        
        <tr>
          <th>Name:</th> <td>${debRepoBean.name}</td><td><p>The repository name forms part of the URL for accessing this repository. Names MUST be unique across a TeamCity 
                    instance and must only contain URL compatible characters.</p></td></tr>
          </td>
        </tr>
        <tr>
          <th>Project:</th> <td>${debRepoBean.project.fullName}</td><td><p>The project this repository belongs to. Users with the Project Administrator Role for this project can edit this repository configuration.</p></td></tr>
          </td>
        </tr>
      </table>

	<br>
 <div class="filterTableContainer">	
    <%@ include file="editDebianRepositoryViewDetails.jsp" %>
</div>
<div class="sidebarAdmin">
<h2>Artifact Filter Configuration</h2>
<h3>Artifact Filename Match (regex)</h3>
<p>This is the regular expression used to find matching artifacts to publish. The filter will be run against the list of artifacts copied to the server</p>

</div>
  </jsp:attribute>
</bs:page>