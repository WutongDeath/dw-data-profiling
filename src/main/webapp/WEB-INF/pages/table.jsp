<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
  <body>
    <h1>Table: ${table.name}</h1>
    <table border="1">
      <tr>
        <th>Column Name</th>
        <th>Type</th>
      </tr>
      <c:forEach var="column" items="${columnList}">
      <tr>
        <td>${column.name}</td>
        <td>${column.type}</td>
      </tr>
      </c:forEach>
    </table>
  </body>
</html>
