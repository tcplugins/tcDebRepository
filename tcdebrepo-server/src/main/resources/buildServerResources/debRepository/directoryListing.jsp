<%@ page language="java" session="true" errorPage="/runtimeError.jsp"
  %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
  %><!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <title>${directoryTitle} : ${currentPathLevel}</title>

    <!-- Bootstrap -->
    <link href="${jspHome}debRepository/css/bootstrap.min.css" rel="stylesheet">
    <link href="${jspHome}debRepository/css/debRepository.css" rel="stylesheet">

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
  </head>
  <body>
    <div class="jumbotron">
      <div class="container">
        <h2 class="directoryTitle">${directoryTitle}</h2>
        <p>Debian (deb) File Repository serving TeamCity build artifacts</p>
      </div>
    </div>
    <div class="container">
   <c:if test="${not empty alertInfo}">
  <div class="alert alert-info" role="alert">
    Add the following to <code>/etc/apt/sources.list</code> to enable this repository:
    <pre><p>${alertInfo}</p></pre>
  </div>
   </c:if>

  <ul class="list-inline indexcrumb">
   <c:forEach items="${breadcrumbItems}" var="linkItem">
   <c:if test="${not empty linkItem.url}">
     <li class="${linkItem.type}"><a href="${linkItem.url}">${linkItem.text}</a></li>
   </c:if>
   <c:if test="${empty linkItem.url}">
     <li class="${linkItem.type}">${linkItem.text}</li>
   </c:if>
   </c:forEach>
  </ul>

   <hr>
  <ul class="list-unstyled">
   <c:forEach items="${linkItems}" var="linkItem">
   <li class="${linkItem.type}"><a href="${linkItem.url}">${linkItem.text}</a></li>
   </c:forEach>
  </ul>
      <hr>
      <footer>
        <p><em>Generated by the <a href="https://github.com/tcplugins/tcDebRepository">${pluginName}</a> 
        plugin (version: ${pluginVersion})</em></p>
      </footer>
    </div>

  </body>
  </html>