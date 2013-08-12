<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/functions" prefix="f" %>
<html>
  <body>
    <h1>Table: ${table.name}</h1>
    <table border="1">
      <tr>
        <th>#</th>
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
        <th>More</th>
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
        <td>${column.nullCount}</td>
        <td>${column.nullPercent}</td>
        <td>${column.distinctValues}</td>
        <td>${column.min}</td>
        <td>${column.max}</td>
        <td>${column.avg}</td>
        <td>${column.sd}</td>
        <td><a href="/table/column/${column.id}">Detail</a></td>
      </tr>
      </c:forEach>
    </table>
  </body>
</html>
