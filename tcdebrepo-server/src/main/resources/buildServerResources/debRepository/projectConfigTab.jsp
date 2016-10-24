<%@ include file="/include.jsp" %>
	<div class="section noMargin">
	
		<h2 class="noBorder">Deb File Repository</h2>
		<div class="grayNote">
		A Deb File Repository is for serving Debian package files (or Ubuntu and other derivative distros).<br>
		
		Creating a repository allows TeamCity to act as a Debian Package Repository for the .deb files produced by your builds.
		</div>
		<c:if test="${debRepoSettings != null}" >
			<p><label for="repo-name">Deb File Repository Name</label>
			<input type="text" name="repo-name" value="${debRepoSettings.repositoryName}"/><br>
			This name forms part of the URL for accessing the repository. Must be a valid URL compatible string.
			</p>
			
			
		</c:if>
	</div>
	