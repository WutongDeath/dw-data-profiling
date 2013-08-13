<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<html>
  <body>
    <h1>Add Table (Connection: <a href="/table/list/${connection.id}">${connection.name}</a>)</h1>

    <form:form action="" method="post" modelAttribute="tableForm">
    <table>
    <tr>
      <td>Table Name:</td>
      <td>
        <form:input path="tableName" />
        <form:errors path="tableName" cssStyle="color: red; font-weight: bold;" />
      </td>
    </tr>
    <tr>
      <td colspan="2">
        <input type="submit" value="Submit">
      </td>
    </tr>
    </table>
    </form:form>
  </body>
</html>
