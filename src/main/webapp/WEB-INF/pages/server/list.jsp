<%@ page contentType="text/html;charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<t:base>

<jsp:body>

  <div class="row-fluid">
    <div class="span2 well" style="padding: 10px 0 10px 0;">
      <ul class="nav nav-list">
        <li class="nav-header">SERVER LIST</li>
        <li><a href="#"><i class="icon-folder-open"></i> dw-master</a></li>
        <li class="active"><a href="#"><i class="icon-blank"></i> <i class="icon-hdd"></i> dw_db</a></li>
        <li><a href="#"><i class="icon-blank"></i> <i class="icon-hdd"></i> dw_stage</a></li>
        <li><a href="#"><i class="icon-blank"></i> <i class="icon-hdd"></i> dw_extract</a></li>
        <li><a href="#"><i class="icon-folder-close"></i> haozu-etl</a></li>
      </ul>
    </div>

    <div class="span10">

      <h3 style="margin-top: 0;">Server List</h3>
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
          <td><a href="/database/list/${server.id}">${server.name}</a></td>
          <td>${server.host}</td>
          <td>${server.port}</td>
          <td>${server.username}</td>
          <td>${server.password}</td>
          <td>${server.databaseCount}</td>
          <td>${server.updated}</td>
          <td>
            <a href="/server/delete/${server.id}" onclick="return confirm('Are you sure to delete this server?');" title="Delete">
              <i class="icon-remove"></i>
            </a>
          </td>
        </tr>
        </c:forEach>
      </table>
      <a href="/server/add" class="btn btn-primary">Add Server</a>

    </div>
  </div>

</jsp:body>

</t:base>
