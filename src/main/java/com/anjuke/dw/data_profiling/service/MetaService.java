package com.anjuke.dw.data_profiling.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.anjuke.dw.data_profiling.dao.DatabaseDao;
import com.anjuke.dw.data_profiling.dao.ServerDao;
import com.anjuke.dw.data_profiling.model.Column;
import com.anjuke.dw.data_profiling.model.Database;
import com.anjuke.dw.data_profiling.model.Server;
import com.anjuke.dw.data_profiling.model.Table;
import com.anjuke.dw.data_profiling.util.Functions;

@Service
public class MetaService {

    @Autowired
    private DatabaseDao databaseDao;

    @Autowired
    private ServerDao serverDao;

    public List<String> getTableNames(int databaseId) {

        Database database = databaseDao.findById(databaseId);
        if (database == null) {
            return null;
        }

        Connection conn = openConnection(databaseId, true);
        if (conn == null) {
            return null;
        }

        try {

            PreparedStatement stmt = conn.prepareStatement("SELECT table_name FROM tables WHERE table_schema = ?");
            stmt.setString(1, database.getName());
            ResultSet rs = stmt.executeQuery();
            List<String> result = new ArrayList<String>();
            while (rs.next()) {
                result.add(rs.getString("table_name"));
            }
            return result;

        } catch (SQLException e) {
            return null;
        } finally {
            closeConnection(conn);
        }

    }

    private Connection openConnection(int databaseId, boolean informationSchema) {

        Database database = databaseDao.findById(databaseId);
        if (database == null) {
            return null;
        }

        Server server = serverDao.findById(database.getServerId());
        if (server == null) {
            return null;
        }

        try {

            String databaseName = informationSchema ? "information_schema" : database.getName();

            return DriverManager.getConnection(
                    String.format(
                            "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=UTF-8",
                            server.getHost(), server.getPort(), databaseName),
                            server.getUsername(), server.getPassword());

        } catch (SQLException e) {
            return null;
        }

    }

    private void closeConnection(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {}
    }

    public Map<String, Map<String, Object>> getTableInfo(int databaseId, List<String> tableNameList) {

        Connection conn = openConnection(databaseId, true);
        if (conn == null) {
            return null;
        }

        try {

            List<String> inList = new ArrayList<String>();
            for (int i = 0; i < tableNameList.size(); ++i) {
                inList.add("?");
            }

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT table_name, table_rows, data_length FROM tables"
                    + " WHERE table_name IN (" + Functions.joinString(inList, ",") + ")");

            for (int i = 0; i < tableNameList.size(); ++i) {
                stmt.setString(i + 1, tableNameList.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            Map<String, Map<String, Object>> result = new HashMap<String, Map<String, Object>>();
            while (rs.next()) {
                Map<String, Object> info = new HashMap<String, Object>();
                info.put("rowCount", rs.getLong("table_rows"));
                info.put("dataLength", rs.getLong("data_length"));
                result.put(rs.getString("table_name"), info);
            }

            stmt = conn.prepareStatement(
                    "SELECT table_name, COUNT(*) AS column_count FROM columns"
                    + " WHERE table_name IN (" + Functions.joinString(inList, ",") + ")"
                    + " GROUP BY table_name");

            for (int i = 0; i < tableNameList.size(); ++i) {
                stmt.setString(i + 1, tableNameList.get(i));
            }

            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> info = result.get(rs.getString("table_name"));
                if (info == null) {
                    continue;
                }
                info.put("columnCount", rs.getInt("column_count"));
            }

            return result;

        } catch (SQLException e) {
            return null;
        } finally {
            closeConnection(conn);
        }

    }

    public void getTableColumnInfo(int databaseId, String tableName, Table table, List<Column> columnList) {

        Database database = databaseDao.findById(databaseId);
        if (database == null) {
            return;
        }

        Connection conn = openConnection(databaseId, true);
        if (conn == null) {
            return;
        }

        try {

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT table_rows, data_length FROM tables"
                    + " WHERE table_schema = ? AND table_name = ?");
            stmt.setString(1, database.getName());
            stmt.setString(2, tableName);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return;
            }
            table.setDatabaseId(database.getId());
            table.setName(tableName);
            table.setRowCount(rs.getLong("table_rows"));
            table.setDataLength(rs.getLong("data_length"));

            stmt = conn.prepareStatement(
                    "SELECT column_name, column_type FROM columns"
                    + " WHERE table_schema = ? AND table_name = ?"
                    + " ORDER BY column_name");
            stmt.setString(1, database.getName());
            stmt.setString(2, tableName);

            rs = stmt.executeQuery();
            while (rs.next()) {
                Column column = new Column();
                column.setName(rs.getString("column_name"));
                column.setType(rs.getString("column_type"));
                column.setTypeFlag(Functions.parseTypeFlag(column.getType()));
                columnList.add(column);
            }

            table.setColumnCount(columnList.size());

        } catch (SQLException e) {
            return;
        } finally {
            closeConnection(conn);
        }

    }

}
