<%@ include file="/include.jsp" %>

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
            <forms:addButton id="addNewFilter" onclick="DebRepoPlugin.addDebRepo('${projectId}'); return false">Add Artifact Filter</forms:addButton>
        </div>
    <%--/c:if --%>
  <div>  
  
  
  <p>Artifact Filters are run against a build when it completes. Any artifacts which match a filter are added to the 
     Debian Repository using the <b>dist</b> and <b>component</b> values specified with the filter. An artifact may 
     match multiple filters, and will be indexed in each category. This is a common case, where an artifact may be 
     compatible with multiple distributions or components.</p> 
  </div>   
     
      <bs:messages key="buildTypesUnassigned"/>
      <bs:messages key="buildTypesAssigned"/>
      
      <c:if test="${filtersNum > 0}">
	      <table class="settings filterTable">
	      <c:forEach items="${debRepoBean.filtersAndBuildTypes}" var="filterTypeEntry">
	      <tr class="filterHeading"><td colspan=4 class="filterTableBuildTitle">${filterTypeEntry.key}</td></tr>
	      	<tr class="filterHeading"><th>Artifact Filename Match (regex)</th><th>Distribution (dist)</th><th colspan=2>Component</th></tr>
	      	<c:forEach items="${filterTypeEntry.value}" var="filterAndBuild">
	      		<c:set var="filter" value="${filterAndBuild.filter}"/>
	      		<tr>
	      			<td>${filter.regex}</td><td>${filter.dist}</td><td>${filter.component}</td>
	      			<td><a id="editDebFilter" href="#" onclick="DebRepoFilterPlugin.editFilter({ uuid: '${debRepoBean.uuid}', name: '${debRepoBean.name}', id: '${filter.id}', dist:'${filter.dist}' }); return false">edit...</a></td>
	      		</tr>     
	      	</c:forEach>
	      	<tr><td colspan=4><a href="">Add Artifact Filter </a></td></tr>
	      	<tr class="blankline"><td colspan=4>&nbsp;</td></tr>
	      </c:forEach>
	      
	      </table>
	  </c:if>
<%--
      <c:set var="canAddRemoveConfigurations" value="true"/>
      <c:set var="addButton"><c:if test="${canAddRemoveConfigurations}">
        <forms:addButton onclick="BS.AttachConfigurationsToClassDialog.showAttachDialog('${debRepo.uuid}'); return false" additionalClasses="add-build-configurations">Add configurations</forms:addButton>
      </c:if></c:set>

      <c:if test="${configurationsNum == 0}">
        <p class="note">There are no configurations added to this priority class.</p>
        <p>${addButton}</p>
      </c:if>
      <c:if test="${configurationsNum > 0}">
        <c:url var="action" value="${teamcityPluginResourcesPath}action.html?detachBuildTypes=true"/>
        <form id="unassignBuildTypesForm" action="${action}" onsubmit="return BS.UnassignBuildTypesForm.submit()">
          <p class="note"><strong>${configurationsNum}</strong> configuration<bs:s val="${configurationsNum}"/> added to this priority class.</p>
          <table class="settings debRepoBuildTypesTable">
            <tr>
              <th class="buildConfigurationName">Build Configuration</th>
              <th class="unassign">
                <forms:checkbox name="selectAll"
                                onmouseover="BS.Tooltip.showMessage(this, {shift: {x: 10, y: 20}, delay: 600}, 'Click to select / unselect all configurations')"
                                onmouseout="BS.Tooltip.hidePopup()"
                                onclick="if (this.checked) BS.UnassignBuildTypesForm.selectAll(true); else BS.UnassignBuildTypesForm.selectAll(false)"/>
              </th>
            </tr>
            <c:forEach items="${sortedBuildTypes}" var="buildType">
              <tr>
                <td>
                  <bs:buildTypeLinkFull buildType="${buildType}"/>
                </td>
                <td class="unassign">
                  <forms:checkbox name="unassign" value="${buildType.id}" />
                </td>
              </tr>
            </c:forEach>
          </table>

          <c:if test="${canAddRemoveConfigurations}">
            <div class="saveButtonsBlock saveButtonsBlockRight">
              ${addButton}
              <forms:saving id="unassignInProgress" className="progressRingInline"/>
              <input class="btn" type="submit" name="detachBuildTypes" value="Remove from priority class"/>
              <input type="hidden" name="pClassId" value="${debRepo.uuid}"/>
            </div>
          </c:if>
        </form>
      </c:if>
      <jsp:include page="${teamcityPluginResourcesPath}attachConfigurationsDialog.html"/>
       --%>
</bs:refreshable>
