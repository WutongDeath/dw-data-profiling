<%@ page contentType="text/html;charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<t:navi>
<jsp:body>

  <h2 class="sub-title">Database List</h2>
  <table class="table table-striped table-hover">
    <tr>
      <th>#</th>
      <th>Database Name</th>
      <th>Table Count</th>
      <th>Last Updated</th>
      <th>Operation</th>
    <tr>
    <c:forEach var="database" items="${databaseList}" varStatus="status">
    <tr>
      <td>${status.index + 1}</td>
      <td><a href="<c:url value="/table/list/${database.id}" />">${database.name}</a></td>
      <td>${database.tableCount}</td>
      <td>${database.updated}</td>
      <td>-</td>
    </tr>
    </c:forEach>
  </table>

</jsp:body>
</t:navi>
