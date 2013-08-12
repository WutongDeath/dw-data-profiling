<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/functions" prefix="f" %>
<html>
  <body>
    <h1>Column: ${column.name} (Table: <a href="/table/view/${table.id}">${table.name}</a>)</h1>

    <h2>General Information:</h2>
    <table border="1">
      <tr>
        <th>Column Name</th>
        <th>Data Type</th>
        <th>Row Count</th>
        <th>Null Count</th>
        <th>Null Percent</th>
        <th>Distinct Values</th>
        <th>Analyze Type</th>
      </tr>
      <tr>
        <td>${column.name}</td>
        <td>${column.type}</td>
        <td>${table.rowCount}</td>
        <td>${generalStats.nullCount}</td>
        <td>${generalStats.nullPercent}</td>
        <td>${generalStats.distinctValues}</td>
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
      </tr>
    </table>

    <c:if test="${not empty numericStats}">
    <h2>Numeric Analyzer</h2>
    <table border="1">
      <tr>
        <th>Min Value</th>
        <th>Max Value</th>
        <th>Avg Value</th>
        <th>Standard Deviation</th>
      </tr>
      <tr>
        <td>${numericStats.min}</td>
        <td>${numericStats.max}</td>
        <td>${numericStats.avg}</td>
        <td>${numericStats.sd}</td>
      </tr>
    </table>
    </c:if>

    <c:if test="${not empty stringStats}">
    <h2>String Analyzer</h2>
    <table border="1">
      <tr>
        <th>Min Length</th>
        <th>Max Length</th>
        <th>Avg Length</th>
      </tr>
      <tr>
        <td>${stringStats.minLength}</td>
        <td>${stringStats.maxLength}</td>
        <td>${stringStats.avgLength}</td>
      </tr>
    </table>
    </c:if>

    <c:if test="${not empty datetimeStats}">
    <h2>Datetime Analyzer</h2>
    <table border="1">
      <tr>
        <th>Min Value</th>
        <th>Max Value</th>
      </tr>
      <tr>
        <td>${datetimeStats.min}</td>
        <td>${datetimeStats.max}</td>
      </tr>
    </table>
    </c:if>
  </body>
</html>
