<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<html>
  <body>
    <h1>Add Server</h1>

    <form:form action="" method="post" modelAttribute="serverForm">
    <table>
    <tr>
      <td>Server Name:</td>
      <td>
        <form:input path="serverName" />
        <form:errors path="serverName"/>
      </td>
    </tr>
    <tr>
      <td>Host:</td>
      <td>
        <form:input path="host" />
        <form:errors path="host"/>
      </td>
    </tr>
    <tr>
      <td>Port:</td>
      <td>
        <form:input path="port" />
        <form:errors path="port"/>
      </td>
    </tr>
    <tr>
      <td>Username:</td>
      <td>
        <form:input path="username" />
        <form:errors path="username"/>
      </td>
    </tr>
    <tr>
      <td>Password:</td>
      <td>
        <form:input path="password" />
        <form:errors path="password"/>
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
