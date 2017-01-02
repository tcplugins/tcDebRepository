<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include-internal.jsp" %>


	        <div class="repoList">
	        <h2 class="noBorder">Debian Package Repositories</h2>
	        
	        <p>There are <a href="debianRepositories.html">${repoCount} repositories</a> 
	        	configured containing ${totalPackageCount} packages.</p>
			<p>Of these, ${packagesAssociated} are associated to a valid repository, and ${packagesUnassociated} are associated with a non-existent repository.</p> 
	        </div>