<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
  <body>
    <h1>Table List (Connection: <a href="/connection/list">${connection.name}</a>)</h1>
  </body>
  <table border="1">
    <tr>
      <th>#</th>
      <th>Table Name</th>
      <th>Column Count</th>
      <th>Row Count</th>
      <th>Data Length</th>
      <th>More</th>
    </tr>
    <c:forEach var="table" items="${tableList}" varStatus="status">
    <tr>
      <td>${status.index + 1}</td>
      <td>${table.name}</td>
      <td>-</td>
      <td>${table.rowCount}</td>
      <td>${table.dataLength}</td>
      <td><a href="/table/view/${table.id}">Detail</a></td>
    </tr>
    </c:forEach>
  </table>
</html>
