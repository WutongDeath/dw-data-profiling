<%@ page contentType="text/html;charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<t:base>

<jsp:attribute name="styles">
<link href="/resources/css/table_list.css" rel="stylesheet">
</jsp:attribute>

<jsp:attribute name="scripts">
<script type="text/javascript" src="/webjars/handlebars/1.0.0/handlebars.min.js"></script>
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

    <form class="form-inline">
      <div class="input-prepend">
        <span class="add-on"><i class="icon-search"></i></span>
        <input id="txtSearch" type="text" placeholder="Table name keyword">
      </div>
      Page: <input type="text" id="txtPage" class="input-mini"> <input type="button" value="Go" id="btnPage" class="btn">
    </form>
    <div class="table-list clearfix" id="divTables">
      <ul id="ulTables"></ul>
    </div>

    <div class="table-info" id="divInfo" style="display: none;"></div>

    <script id="tplInfo" type="text/x-handlebars-template">
      <div class="clearfix">
        <h2 class="sub-title pull-left">Table: {{tableName}}</h2>
        <a href="javascript:void(0);" class="btn btn-link pull-left" back="back" style="margin: 7px 15px 0 0;">Back</a>
      </div>

      <table class="table">
        <tr>
          <th>Server</th>
          <th>Database</th>
          <th>Table</th>
          <th>Columns</th>
          <th>Rows</th>
          <th>Size</th>
          <th>Status</th>
          <th>Last Updated</th>
          <th>Operation</th>
        </tr>
        <tr>
          <td>{{serverName}}</td>
          <td>{{databaseName}}</td>
          <td>{{tableName}}</td>
          <td>{{columnCount}}</td>
          <td>{{rowCount}}</td>
          <td>{{dataLength}}</td>
          <td><span class="label label-success">Processed</span></td>
          <td>{{updated}}</td>
          <td>
            <a href="#" class="btn btn-small">Profiling</a>
          </td>
        </tr>
      </table>

      <h2 class="sub-title">Column Information</h2>
      <table class="table">
        <tr>
          <th valign="center">#</th>
          <th>Column Name</th>
          <th>Data Type</th>
          <th>Analyze Type</th>
          <th>Null<br>Count</th>
          <th>Null<br>Percent</th>
          <th>Distinct<br>Values</th>
          <th>Min Value<br>or Length</th>
          <th>Max Value<br>or Length</th>
          <th>Avg Value<br>or Length</th>
          <th>Standard<br>Deviation</th>
          <th>Operation</th>
        </tr>
        {{#each columnList}}
        <tr>
          <td>{{columnIndex}}</td>
          <td>{{columnName}}</td>
          <td>{{columnType}}</td>
          <td><a href="javascript:void(0);"><i class="icon-edit"></i></a> {{typeFlagString}}</td>
          <td>{{nullCount}}</td>
          <td>{{nullPercent}}</td>
          <td>{{distinctValues}}</td>
          <td>{{min}}</td>
          <td>{{max}}</td>
          <td>{{avg}}</td>
          <td>{{sd}}</td>
          <td><a href="#" class="btn btn-small">Details</a></td>
        </tr>
        {{/each}}
      </table>
    </script>

  </jsp:body>
  </t:navi>
</jsp:body>
</t:base>
