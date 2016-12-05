<%@ include file="/include.jsp" %>
<%@ taglib prefix="myt" uri="/plugins/tcDebRepository/debRepository/tag-lib/mytaglib.tld" %>

<c:set var="filtersNum" value="${fn:length(debRepoBean.filtersAndBuildTypes)}"/>
<bs:refreshable containerId="repoBuildTypesContainer" pageUrl="${pageUrl}">
  <a name="artifactFilters"></a><h2 class="title_underlined">Artifact Filters</h2>
      <c:if test="${filtersNum == 0}">
      
    	<div class="icon_before icon16 attentionComment clearfix noFilterWarning">There are no Artifact Filters configured for this Debian Repository yet.
          Please add one or more Artifact Filters to enable TeamCity build artifacts to be indexed by this repository.
	    </div>
      </c:if>
      
    <%--c:if test="${userHasPermissionManagement}" --%>
        <div class="add addNewFilter">
            <forms:addButton id="addNewFilter" onclick="DebRepoFilterPlugin.addFilter({ uuid: '${repoConfig.uuid}', name: '${debRepoBean.name}', id: '_new', build: '', regex: '', dist:'', component:'' }); return false">Add Artifact Filter</forms:addButton>
        </div>
    <%--/c:if --%>
  <div>  
  
  
  <p>Artifact Filters are run against a build when it completes. Any artifacts which match a filter are added to the 
     Debian Repository using the <b>dist</b> and <b>component</b> values specified with the filter. An artifact may 
     match multiple filters, and will be indexed in each category. This is a common use case, where an artifact may be 
     compatible with multiple distributions or components.</p> 
  </div>   
     
      <bs:messages key="buildTypesUnassigned"/>
      <bs:messages key="filterUpdateResult"/>
      <bs:messages key="repoUpdateResult"/>
      
      <c:if test="${filtersNum > 0}">
	      <table class="settings filterTable">
	      <c:set var="buildTypeId" value=""/>
	      <c:forEach items="${debRepoBean.filtersAndBuildTypes}" var="filterTypeEntry">
	      <tr class="filterHeading"><td colspan=5 class="filterTableBuildTitle">${filterTypeEntry.key}</td></tr>
	      	<tr class="filterHeading"><th>Artifact Filename Match (regex)</th><th>Distribution (dist)</th><th colspan=3>Component</th></tr>
	      	<c:forEach items="${filterTypeEntry.value}" var="filterAndBuild">
	      		<c:set var="filter" value="${filterAndBuild.filter}"/>
	      		<c:set var="buildTypeId" value="${filterAndBuild.buildTypeId}"/>
	      		<tr>
	      			<td>${filter.regex}</td><td>${filter.dist}</td><td>${filter.component}</td>
	      			<td><a id="editDebFilter" href="#" onclick="DebRepoFilterPlugin.editFilter({ uuid: '${debRepoBean.uuid}', name: '${debRepoBean.name}', id: '${filter.id}', build: '${buildTypeId}', regex: '${myt:escapeJS(filter.regex)}', dist:'${filter.dist}', component:'${filter.component}' }); return false">edit</a></td>
	      			<td><a id="deleteDebFilter" href="#" onclick="DebRepoFilterPlugin.deleteFilter({ uuid: '${debRepoBean.uuid}', name: '${debRepoBean.name}', id: '${filter.id}', build: '${buildTypeId}' }); return false">delete</a></td>
	      		</tr>     
	      	</c:forEach>
	      	<tr><td colspan=5><a href="#" onclick="DebRepoFilterPlugin.addFilter({ uuid: '${repoConfig.uuid}', name: '${debRepoBean.name}', id: '_new', build: '${buildTypeId}', regex: '', dist:'', component:'' }); return false">Add Artifact Filter </a></td></tr>
	      	<tr class="blankline"><td colspan=5>&nbsp;</td></tr>
	      </c:forEach>
	      
	      </table>
	  </c:if>
</bs:refreshable>
