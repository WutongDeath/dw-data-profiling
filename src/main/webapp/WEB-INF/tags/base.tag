<%@ tag pageEncoding="UTF-8"%>
<%@ attribute name="styles" fragment="true" %>
<%@ attribute name="scripts" fragment="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="content-type" content="text/html;charset=utf-8">
    <title>Data Profiling</title>
    <link href="<c:url value="/webjars/bootstrap/2.3.2/css/bootstrap.min.css" />" rel="stylesheet">
    <link href="<c:url value="/webjars/bootstrap/2.3.2/css/bootstrap-responsive.min.css" />" rel="stylesheet">
    <link href="<c:url value="/resources/css/base.css" />" rel="stylesheet">
    <jsp:invoke fragment="styles"/>
  </head>
  <body>
    <div class="container-fluid">
      <div class="row-fluid">
        <div class="span12">
          <h1 class="site-title">Data Profiling</h1>
        </div>
      </div>

      <jsp:doBody/>
    </div>

    <script type="text/javascript" src="<c:url value="/webjars/jquery/1.10.2/jquery.min.js" />"></script>
    <script type="text/javascript" src="<c:url value="/webjars/bootstrap/2.3.2/js/bootstrap.min.js" />"></script>
    <jsp:invoke fragment="scripts"/>
  </body>
</html>
