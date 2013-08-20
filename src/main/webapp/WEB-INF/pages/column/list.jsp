<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/functions" prefix="f" %>
<html>
  <body>
    <h1>Table: ${table.name} (Database: <a href="/table/list/${database.id}">${database.name}</a>)</h1>
    <p><b>Row count:</b> ${table.rowCount}</p>
    <table border="1">
      <tr>
        <th>#</th>
        <th>Column Name</th>
        <th>Data Type</th>
        <th>Analyze Type</th>

        <c:if test="${profiled}">
        <th>Null<br>Count</th>
        <th>Null<br>Percent</th>
        <th>Distinct<br>Values</th>
        <th>Min Value<br>or Length</th>
        <th>Max Value<br>or Length</th>
        <th>Avg Value<br>or Length</th>
        <th>Standard<br>Deviation</th>
        <th>More</th>
        </c:if>

      </tr>
      <c:forEach var="column" items="${columnList}" varStatus="status">
      <tr>
        <td>${status.index + 1}</td>
        <td>${column.name}</td>
        <td>${column.type}</td>
        <td>
          <c:forEach var="entry" items="${typeFlagMap}">
          <label>
            <input type="checkbox" name="type_flag" column_id="${column.id}" value="${entry.key}"
            <c:if test="${f:bitwiseAnd(column.typeFlag, entry.key) == entry.key}">checked="checked"</c:if>
            >
            ${entry.value}
          </label>
          </c:forEach>
        </td>

        <c:if test="${profiled}">
        <td>${columnStats[column.id].nullCount}</td>
        <td>${columnStats[column.id].nullPercent}</td>
        <td>${columnStats[column.id].distinctValues}</td>
        <td>${columnStats[column.id].min}</td>
        <td>${columnStats[column.id].max}</td>
        <td>${columnStats[column.id].avg}</td>
        <td>${columnStats[column.id].sd}</td>
        <td><a href="/table/column/${column.id}">Detail</a></td>
        </c:if>

      </tr>
      </c:forEach>
    </table>
  </body>
</html>