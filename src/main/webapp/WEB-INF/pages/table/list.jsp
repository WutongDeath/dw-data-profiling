<%@ page contentType="text/html;charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<t:base>

<jsp:attribute name="styles">
<link href="/resources/css/table_list.css" rel="stylesheet">
</jsp:attribute>

<jsp:attribute name="scripts">
<script type="text/javascript" src="/resources/js/table_list.js"></script>
<script type="text/javascript">
new TableList({
  databaseId: ${database.id},
  tableNameList: ${tableNameList}
});
</script>
</jsp:attribute>

<jsp:body>
  <t:navi>
  <jsp:body>

    <div class="input-prepend">
      <span class="add-on"><i class="icon-search"></i></span>
      <input id="txtSearch" type="text" placeholder="Table name keyword">
    </div>

    <div class="table-list">
      <ul id="ulTables"></ul>
    </div>

    <p>Page: <input type="text" id="txtPage"> <input type="button" value="Go" id="btnPage"></p>

  </jsp:body>
  </t:navi>
</jsp:body>
</t:base>
