<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
  <body>
    <h1>Server List</h1>
  </body>
  <table border="1">
    <tr>
      <th>#</th>
      <th>Server Name</th>
      <th>Host</th>
      <th>Port</th>
      <th>Username</th>
      <th>Password</th>
      <th>Database Count</th>
      <th>Last Updated</th>
      <th>Operation</th>
    </tr>
    <c:forEach var="server" items="${serverList}" varStatus="status">
    <tr>
      <td>${status.index + 1}</td>
      <td><a href="/database/list/${server.id}">${server.name}</a></td>
      <td>${server.host}</td>
      <td>${server.port}</td>
      <td>${server.username}</td>
      <td>${server.password}</td>
      <td>${server.databaseCount}</td>
      <td>${server.updated}</td>
      <td>
        <a href="/server/delete/${server.id}" onclick="return confirm('Are you sure to delete this server?');">Delete</a>
      </td>
    </tr>
    </c:forEach>
  </table>
  <a href="/server/add">Add Server</a>
</html>
