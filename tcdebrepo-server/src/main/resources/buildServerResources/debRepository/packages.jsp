<%@ page contentType="text/plain;charset=UTF-8" language="java" session="true" errorPage="/runtimeError.jsp"
  %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
  %><c:forEach items="${packages}" var="pack"><c:forEach items="${pack.parameters}" var="parameter" 
  >${parameter.name}: ${parameter.value}
</c:forEach>

</c:forEach>