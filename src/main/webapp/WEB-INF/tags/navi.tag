<%@ tag pageEncoding="UTF-8"%>
<%@ attribute name="styles" fragment="true" %>
<%@ attribute name="scripts" fragment="true" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<t:base>

<jsp:attribute name="styles">
  <jsp:invoke fragment="styles"/>
</jsp:attribute>

<jsp:attribute name="scripts">
  <script type="text/javascript">
  $('#ulNavi li[is_server] a i').click(function() {
    var op = $(this).is('.icon-folder-open') ? ['open', 'close', 'hide'] : ['close', 'open', 'show'];
    $(this).removeClass('icon-folder-' + op[0]).addClass('icon-folder-' + op[1]);
    $('#ulNavi li[server_id="' + $(this).parent().parent().attr('is_server') + '"]')[op[2]]();
    return false;
  });
  </script>
  <jsp:invoke fragment="scripts"/>
</jsp:attribute>

<jsp:body>
<div class="row-fluid">
  <div class="span2 well" style="padding: 10px 0 10px 0;">
    <ul class="nav nav-list" id="ulNavi">
      <li><a href="<c:url value="/server/list" />" style="font-weight: bold; color: gray;">Servers</a></li>

      <c:forEach var="server" items="${navi}">
        <li is_server="${server.id}" ${server.isChosen ? "class=\"active\"" : ""}>
          <a href="<c:url value="/database/list/${server.id}" />">
            <i class="icon-folder-${server.isExpanded ? "open" : "close"}"></i>
            ${server.name}
          </a>
        </li>
        <c:forEach var="database" items="${server.databaseList}">
          <li
          server_id="${server.id}"
          ${server.isExpanded ? "" : "style=\"display: none;\""}
          ${database.isChosen ? "class=\"active\"" : ""}
          >
            <a href="<c:url value="/table/list/${database.id}" />">
              <i class="icon-blank"></i> <i class="icon-hdd"></i>
              ${database.name}
            </a>
          </li>
        </c:forEach>
      </c:forEach>
    </ul>
  </div>

  <div class="span10">
    <jsp:doBody/>
  </div>
</div>
</jsp:body>

</t:base>
