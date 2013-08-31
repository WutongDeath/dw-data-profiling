<%@ page contentType="text/html;charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<t:navi>

<jsp:attribute name="styles">
<link href="<c:url value="/resources/css/table_list.css" />" rel="stylesheet">
</jsp:attribute>

<jsp:attribute name="scripts">
<script type="text/javascript" src="<c:url value="/webjars/highcharts/3.0.1/highcharts.js" />"></script>
<script type="text/javascript" src="<c:url value="/webjars/handlebars/1.0.0/handlebars.min.js" />"></script>
<script type="text/javascript" src="<c:url value="/resources/js/table_list.js" />"></script>
<script type="text/javascript">
new TableList({
  databaseId: ${database.id},
  tableNameList: ${tableNameList},
  contextPath: '${pageContext.request.contextPath}'
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

<div class="modal hide" id="dlgDetails">
  <div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3></h3>
  </div>
  <div class="modal-body"></div>
  <div class="modal-footer">
    <a href="javascript:void(0);" class="btn" data-dismiss="modal">Close</a>
  </div>
</div>

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
          <td id="tdStatus"></td>
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
          <td><span comment="comment" title="{{comment}}">{{columnName}}</span></td>
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
          <td>
            {{#if hasDetails}}
            <a href="javascript:void(0);" class="btn btn-small" details="{{columnId}}">Details</a>
            {{else}}
            <a href="javascript:void(0);" class="btn btn-small disabled">Details</a>
            {{/if}}
          </td>
        </tr>
        {{/each}}
      </table>
    </script>

    <script id="tplDetails" type="text/x-handlebars-template">
    <h4>General Information</h4>
    <table class="table table-condensed">
      <tr>
        <th>Data Type</th>
        <th>Row Count</th>
        <th>Null Count</th>
        <th>Null Percent</th>
        <th>Distinct Values</th>
        <th>Analyze Type</th>
      </tr>
      <tr>
        <td>{{columnType}}</td>
        <td>{{rowCount}}</td>
        <td>{{generalStats.nullCount}}</td>
        <td>{{generalStats.nullPercent}}</td>
        <td>{{generalStats.distinctValues}}</td>
        <td>{{typeFlagString}}</td>
      </tr>
    </table>

    {{#if hasNumericStats}}
    <h4>Numeric Analyzer</h4>
    <table class="table table-condensed">
      <tr>
        <th>Min Value</th>
        <th>Max Value</th>
        <th>Avg Value</th>
        <th>Standard Deviation</th>
      </tr>
      <tr>
        <td>{{numericStats.min}}</td>
        <td>{{numericStats.max}}</td>
        <td>{{numericStats.avg}}</td>
        <td>{{numericStats.sd}}</td>
      </tr>
      <tr><td colspan="4"><div id="divNumericTop10" style="width: 480px; height: 300px;"></div></td></tr>
      <tr><th>Bottom 10</th><td colspan="3">{{numericStats.bottom10String}}</td></tr>
    </table>
    {{/if}}

    {{#if hasStringStats}}
    <h4>String Analyzer</h4>
    <table class="table table-condensed">
      <tr>
        <th>Min Length</th>
        <th>Max Length</th>
        <th>Avg Length</th>
        <th>&nbsp;</th>
      </tr>
      <tr>
        <td>{{stringStats.minLength}}</td>
        <td>{{stringStats.maxLength}}</td>
        <td>{{stringStats.avgLength}}</td>
        <td>&nbsp;</td>
      </tr>
      <tr>
        <th colspan="2">Top 10</th>
        <th colspan="2">Bottom 10</th>
      </tr>
      <tr>
        <td colspan="2">{{{stringStats.top10String}}}</td>
        <td colspan="2">{{{stringStats.bottom10String}}}</td>
      </tr>
    </table>
    {{/if}}

    {{#if hasDatetimeStats}}
    <h4>Datetime Analyzer</h4>
    <table class="table table-condensed">
      <tr>
        <th colspan="2">Min Value</th>
        <th colspan="2">Max Value</th>
      </tr>
      <tr>
        <td colspan="2">{{datetimeStats.min}}</td>
        <td colspan="2">{{datetimeStats.max}}</td>
      </tr>
      <tr>
        <th>Min Date</th>
        <th>Max Date</th>
        <th>Min Time</th>
        <th>Max Time</th>
      </tr>
      <tr>
        <td>{{datetimeStats.minDate}}</td>
        <td>{{datetimeStats.maxDate}}</td>
        <td>{{datetimeStats.minTime}}</td>
        <td>{{datetimeStats.maxTime}}</td>
      </tr>
      <tr>
        <th colspan="2">Top 10</th>
        <th colspan="2">Bottom 10</th>
      </tr>
      <tr>
        <td colspan="2">{{{datetimeStats.top10String}}}</td>
        <td colspan="2">{{{datetimeStats.bottom10String}}}</td>
      </tr>
    </table>
    {{/if}}
    </script>

</jsp:body>
</t:navi>
