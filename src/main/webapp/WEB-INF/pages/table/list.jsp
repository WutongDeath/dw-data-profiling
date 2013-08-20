<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
  <body>
    <h1>Table List (Database: <a href="/database/list/${database.serverId}">${database.name}</a>)</h1>

    <p>Search: <input type="text" id="txtSearch"></p>

    <table border="1" id="tblTables">
      <tr>
        <th>#</th>
        <th>Table Name</th>
        <th>Column Count</th>
        <th>Row Count</th>
        <th>Data Length</th>
        <th>Operation</th>
      </tr>
      <tr>
        <td colspan="6" align="center">
          Loading...
        </td>
      </tr>
    </table>

    <p>Page: <input type="text" id="txtPage"> <input type="button" value="Go" id="btnPage"></p>

    <script type="text/javascript" src="/webjars/jquery/1.10.2/jquery.min.js"></script>
    <script type="text/javascript" src="/resources/js/table_list.js"></script>
    <script type="text/javascript">
    new TableList({
      databaseId: ${database.id},
      tableNameList: ${tableNameList}
    });
    </script>
  </body>
</html>
