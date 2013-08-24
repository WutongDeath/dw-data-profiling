<%@ page contentType="text/html;charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<t:navi>

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

    <form class="form-inline">
      <div class="input-prepend">
        <span class="add-on"><i class="icon-search"></i></span>
        <input id="txtSearch" type="text" placeholder="Table name keyword">
      </div>
    </form>

    <div id="divTables">
      <ul class="table-list clearfix" id="ulTables"></ul>
      <div class="pagination">
        <ul>
          <li><a href="#">Prev</a></li>
          <li><a href="#">1</a></li>
          <li><a href="#">2</a></li>
          <li><a href="#">3</a></li>
          <li><a href="#">4</a></li>
          <li><a href="#">5</a></li>
          <li><a href="#">Next</a></li>
        </ul>
      </div>
    </div>

    <div class="table-info" id="divInfo" style="display: none;"></div>

    <script id="tplInfo" type="text/x-handlebars-template">
      <div class="clearfix">
        <h2 class="sub-title pull-left">Table: {{tableName}}</h2>
        <a href="javascript:void(0);" class="btn btn-link pull-left" back="back" style="margin: 7px 0 0 20px;">Back</a>
        <a href="javascript:void(0);" class="btn btn-link pull-left" refresh="refresh" style="margin-top: 7px;">Refresh</a>
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
          <td><span class="label" id="lblStatus"></span></td>
          <td>{{updated}}</td>
          <td>
            <a href="javascript:void(0);" class="btn btn-small" id="btnProfiling">Profiling</a>
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
          <th>Last Updated</th>
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
          <td>{{updated}}</td>
          <td><a href="#" class="btn btn-small">Details</a></td>
        </tr>
        {{/each}}
      </table>
    </script>

</jsp:body>
</t:navi>
