<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
  <body>
    <h1>Connection List</h1>
    <table border="1">
      <tr>
        <th>Name</th>
        <th>JDBC Url</th>
        <th>Username</th>
      <tr>
      <c:forEach var="connection" items="${connectionList}">
      <tr>
        <td><a href="/table/list/${connection.id}">${connection.name}</a></td>
        <td>jdbc:mysql://${connection.host}:${connection.port}/${connection.database}</td>
        <td>${connection.username}</td>
      </tr>
      </c:forEach>
    </table>
  </body>
</html>
