<%@ page contentType="text/html;charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<t:navi>

<jsp:attribute name="scripts">
<script type="text/javascript" src="<c:url value="/resources/js/server_list.js" />"></script>
<script type="text/javascript">
new ServerList({
    contextPath: '${pageContext.request.contextPath}'
});
</script>
</jsp:attribute>

<jsp:body>

  <h2 class="sub-title">Server List</h2>
  <table class="table table-striped table-hover">
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
      <td><a href="<c:url value="/database/list/${server.id}" />">${server.name}</a></td>
      <td>${server.host}</td>
      <td>${server.port}</td>
      <td>${server.username}</td>
      <td>${server.password}</td>
      <td>${server.databaseCount}</td>
      <td>${server.updated}</td>
      <td>
        <a href="<c:url value="/server/delete/${server.id}" />" onclick="return confirm('Are you sure to delete this server?');" title="Delete">
          <i class="icon-remove"></i>
        </a>
      </td>
    </tr>
    </c:forEach>
  </table>
  <a href="#dlgServer" data-toggle="modal" class="btn btn-primary">Add Server</a>

<div class="modal hide fade" id="dlgServer">
  <div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3>Add Server</h3>
  </div>
  <div class="modal-body">

<form class="form-horizontal">
  <div class="control-group">
    <label class="control-label">Server Name</label>
    <div class="controls">
      <input type="text" name="serverName">
    </div>
  </div>
  <div class="control-group">
    <label class="control-label">Host</label>
    <div class="controls">
      <input type="text" name="host">
    </div>
  </div>
  <div class="control-group">
    <label class="control-label">Port</label>
    <div class="controls">
      <input type="text" name="port">
    </div>
  </div>
  <div class="control-group">
    <label class="control-label">Username</label>
    <div class="controls">
      <input type="text" name="username">
    </div>
  </div>
  <div class="control-group">
    <label class="control-label">Password</label>
    <div class="controls">
      <input type="text" name="password">
    </div>
  </div>
</form>

  </div>
  <div class="modal-footer">
    <a href="javascript:void(0);" class="btn btn-primary" id="btnAdd">Submit</a>
    <a href="javascript:void(0);" class="btn" data-dismiss="modal">Close</a>
  </div>
</div>

</jsp:body>
</t:navi>
