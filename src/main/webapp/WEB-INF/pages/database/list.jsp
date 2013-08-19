<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
  <body>
    <h1>Database List (Server: <a href="/server/list">${server.name}</a>)</h1>
    <table border="1">
      <tr>
        <th>Database Name</th>
        <th>Table Count</th>
        <th>Last Updated</th>
        <th>Operation</th>
      <tr>
      <c:forEach var="database" items="${databaseList}">
      <tr>
        <td><a href="/table/list/${database.id}">${database.name}</a></td>
        <td>${database.tableCount}</td>
        <td>${database.updated}</td>
        <td>-</td>
      </tr>
      </c:forEach>
    </table>
  </body>
</html>
