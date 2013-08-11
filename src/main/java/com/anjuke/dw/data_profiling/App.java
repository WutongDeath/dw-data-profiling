package com.anjuke.dw.data_profiling;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONValue;

public class App implements Runnable {

    private Logger logger = Logger.getLogger(App.class);

    private Connection connStats;

    private SimpleDateFormat dfDatetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat dfTime = new SimpleDateFormat("HH:mm:ss");

    private void connectStats() {

        if (connStats == null) {
            try {
                connStats = DriverManager.getConnection("jdbc:mysql://localhost/dwms_db?useUnicode=true&characterEncoding=UTF-8", "dwms_db", "dwms_db");
                connStats.setAutoCommit(true);
                logger.info("Connected to Stats.");
            } catch (SQLException e) {
                logger.error("Fail to connect Stats.", e);
            }
            return;
        }

        try {
            PreparedStatement stmt = connStats.prepareStatement("/* ping */ SELECT 1");
            stmt.execute();
        } catch (SQLException e) {
            logger.warn("Lost connection to Stats. Reconnecting...", e);
            try {
                connStats.close();
            } catch (SQLException e1) {};
            connStats = null;
            connectStats();
        }

    }

    private class QueueItem {
        public int id;
        public int tableId;
    }

    private class Column {
        public int id;
        public String name;
        public int typeFlag;
    }

    private List<QueueItem> fetchQueue() {

        List<QueueItem> queue = new ArrayList<QueueItem>();

        try {
            PreparedStatement stmt = connStats.prepareStatement("SELECT id, table_id FROM dp_update_queue WHERE status = 1 ORDER BY id");
            ResultSet result = stmt.executeQuery();
            while (result.next()) {
                QueueItem item = new QueueItem();
                item.id = result.getInt(1);
                item.tableId = result.getInt(2);
                queue.add(item);
            }
        } catch (SQLException e) {
            logger.error("Fail to fetch queue.", e);
            return null;
        }

        return queue;
    }

    private void updateStatus(QueueItem item, int status) {

        try {

            PreparedStatement stmt = connStats.prepareStatement("UPDATE dp_update_queue SET status = ? WHERE id = ? LIMIT 20");
            stmt.setInt(1, status);
            stmt.setInt(2, item.id);
            if (stmt.executeUpdate() == 0) {
                throw new SQLException("Affected rows is zero.");
            }

        } catch (SQLException e) {
            logger.error("Fail to update queue item id " + item.id, e);
        }
    }

    private boolean process(QueueItem item) {

        logger.info(String.format("Processing queue item id: %d, table id: %d.", item.id, item.tableId));

        int connectionId = 0;
        String tableName = null;
        try {

            PreparedStatement stmtTable = connStats.prepareStatement("SELECT connection_id, name FROM dp_table WHERE id = ?");
            stmtTable.setInt(1, item.tableId);
            ResultSet rsTable = stmtTable.executeQuery();
            rsTable.next();
            connectionId = rsTable.getInt(1);
            tableName = rsTable.getString(2);

        } catch (SQLException e) {
            logger.error("Unable to get table information.", e);
            return false;
        }

        Connection connTarget = null;
        try {
            PreparedStatement stmtConnection = connStats.prepareStatement("SELECT host, port, username, password, `database` FROM dp_connection WHERE id = ?");
            stmtConnection.setInt(1, connectionId);
            ResultSet rsConnection = stmtConnection.executeQuery();
            rsConnection.next();
            connTarget = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=UTF-8",
                            rsConnection.getString(1), rsConnection.getInt(2), rsConnection.getString(5)),
                    rsConnection.getString(3), rsConnection.getString(4));
        } catch (SQLException e) {
            logger.error("Unable to connect target database.", e);
            return false;
        }

        long rows = 0;
        long dataLength = 0;
        try {
            PreparedStatement stmtTableStatus = connTarget.prepareStatement("SHOW TABLE STATUS LIKE ?");
            stmtTableStatus.setString(1, tableName);
            ResultSet rsTableStatus = stmtTableStatus.executeQuery();
            rsTableStatus.next();
            rows = rsTableStatus.getLong("rows");
            dataLength = rsTableStatus.getLong("data_length");
        } catch (SQLException e) {
            logger.error("Unable to get table status.");
            return false;
        }

        try {
            PreparedStatement stmtUpdateTableStatus = connStats.prepareStatement("UPDATE dp_table SET row_count = ?, data_length = ? WHERE id = ?");
            stmtUpdateTableStatus.setLong(1, rows);
            stmtUpdateTableStatus.setLong(2, dataLength);
            stmtUpdateTableStatus.setInt(3, item.tableId);
            if (stmtUpdateTableStatus.executeUpdate() == 0) {
                throw new SQLException("Affected rows is zero.");
            }
        } catch (SQLException e) {
            logger.error("Unable to update table status.");
            return false;
        }

        List<Column> columnList = new ArrayList<Column>();
        try {
            PreparedStatement stmtColumns = connStats.prepareStatement("SELECT id, name, type_flag FROM dp_column WHERE table_id = ? AND type_flag > 0");
            stmtColumns.setInt(1, item.tableId);
            ResultSet rsColumns = stmtColumns.executeQuery();
            while (rsColumns.next()) {
                Column column = new Column();
                column.id = rsColumns.getInt(1);
                column.name = rsColumns.getString(2);
                column.typeFlag = rsColumns.getInt(3);
                columnList.add(column);
            }
            if (columnList.size() == 0) {
                throw new SQLException("Column list is empty.");
            }
        } catch (SQLException e) {
            logger.error("Unable to fetch column list.", e);
            return false;
        }

        boolean success = true;
        for (Column column : columnList) {
            if (!processColumn(connTarget, tableName, column)) {
                success = false;
            }
        }

        return success;

    }

    private boolean processColumn(Connection connTarget, String tableName, Column column) {

        Map<String, Map<String, Object>> stats = new HashMap<String, Map<String, Object>>();
        PreparedStatement stmt;
        ResultSet result;

        try {

            // general
            Map<String, Object> generalStats = new HashMap<String, Object>();
            stats.put("general", generalStats);

            stmt = connTarget.prepareStatement(String.format("SELECT COUNT(*) FROM %s WHERE %s IS NULL", tableName, column.name));
            result = stmt.executeQuery();
            result.next();
            generalStats.put("null", result.getLong(1));

            stmt = connTarget.prepareStatement(String.format("SELECT COUNT(DISTINCT %s) FROM %s", column.name, tableName));
            result = stmt.executeQuery();
            result.next();
            generalStats.put("distinct", result.getLong(1));

            // numeric
            if ((column.typeFlag & 1) == 1) {

                Map<String, Object> numericStats = new HashMap<String, Object>();
                stats.put("numeric", numericStats);

                stmt = connTarget.prepareStatement(
                        String.format("SELECT MIN(%s), MAX(%s), AVG(%s), STDDEV_POP(%s) FROM %s",
                                column.name, column.name, column.name, column.name, tableName));
                result = stmt.executeQuery();
                result.next();
                numericStats.put("min", result.getDouble(1));
                numericStats.put("max", result.getDouble(2));
                numericStats.put("avg", result.getDouble(3));
                numericStats.put("sd", result.getDouble(4));

                stmt = connTarget.prepareStatement(
                        String.format("SELECT %s, COUNT(*) FROM %s GROUP BY %s ORDER BY COUNT(*) DESC LIMIT 10",
                                column.name, tableName, column.name));
                result = stmt.executeQuery();
                List<Object> numericTop10 = new ArrayList<Object>();
                while (result.next()) {
                    numericTop10.add(result.getObject(1));
                    numericTop10.add(result.getLong(2));
                }
                numericStats.put("top10", numericTop10);

                stmt = connTarget.prepareStatement(
                        String.format("SELECT %s, COUNT(*) FROM %s GROUP BY %s ORDER BY COUNT(*) LIMIT 10",
                                column.name, tableName, column.name));
                result = stmt.executeQuery();
                List<Object> numericBottom10 = new ArrayList<Object>();
                while (result.next()) {
                    numericBottom10.add(result.getObject(1));
                    numericBottom10.add(result.getLong(2));
                }
                numericStats.put("bottom10", numericBottom10);

            }

            // string
            if ((column.typeFlag & 2) == 2) {

                Map<String, Object> stringStats = new HashMap<String, Object>();
                stats.put("string", stringStats);

                stmt = connTarget.prepareStatement(
                        String.format("SELECT MIN(LENGTH(%s)), MAX(LENGTH(%s)), AVG(LENGTH(%s)) FROM %s",
                                column.name, column.name, column.name, tableName));
                result = stmt.executeQuery();
                result.next();
                stringStats.put("min_length", result.getInt(1));
                stringStats.put("max_length", result.getInt(2));
                stringStats.put("avg_length", result.getInt(3));

                stmt = connTarget.prepareStatement(
                        String.format("SELECT %s, COUNT(*) FROM %s GROUP BY %s ORDER BY COUNT(*) DESC LIMIT 10",
                                column.name, tableName, column.name));
                result = stmt.executeQuery();
                List<Object> stringTop10 = new ArrayList<Object>();
                while (result.next()) {
                    stringTop10.add(result.getObject(1));
                    stringTop10.add(result.getLong(2));
                }
                stringStats.put("top10", stringTop10);

                stmt = connTarget.prepareStatement(
                        String.format("SELECT %s, COUNT(*) FROM %s GROUP BY %s ORDER BY COUNT(*) LIMIT 10",
                                column.name, tableName, column.name));
                result = stmt.executeQuery();
                List<Object> stringBottom10 = new ArrayList<Object>();
                while (result.next()) {
                    stringBottom10.add(result.getObject(1));
                    stringBottom10.add(result.getLong(2));
                }
                stringStats.put("bottom10", stringBottom10);

            }

            // datetime
            if ((column.typeFlag & 4) == 4) {

                Map<String, Object> datetimeStats = new HashMap<String, Object>();
                stats.put("datetime", datetimeStats);

                stmt = connTarget.prepareStatement(
                        String.format("SELECT MIN(%s), MAX(%s), MIN(DATE(%s)), MAX(DATE(%s)), MIN(TIME(%s)), MAX(TIME(%s)) FROM %s",
                                column.name, column.name, column.name, column.name, column.name, column.name, tableName));
                result = stmt.executeQuery();
                result.next();
                datetimeStats.put("min", dfDatetime.format(result.getTimestamp(1)));
                datetimeStats.put("max", dfDatetime.format(result.getTimestamp(2)));
                datetimeStats.put("min_date", dfDate.format(result.getDate(3)));
                datetimeStats.put("max_date", dfDate.format(result.getDate(4)));
                datetimeStats.put("min_time", dfTime.format(result.getTime(5)));
                datetimeStats.put("max_time", dfTime.format(result.getTime(6)));

                stmt = connTarget.prepareStatement(
                        String.format("SELECT DATE(%s), COUNT(*) FROM %s GROUP BY DATE(%s) ORDER BY COUNT(*) DESC LIMIT 10",
                                column.name, tableName, column.name));
                result = stmt.executeQuery();
                List<Object> datetimeTop10 = new ArrayList<Object>();
                while (result.next()) {
                    datetimeTop10.add(result.getObject(1));
                    datetimeTop10.add(result.getLong(2));
                }
                datetimeStats.put("top10", datetimeTop10);

                stmt = connTarget.prepareStatement(
                        String.format("SELECT DATE(%s), COUNT(*) FROM %s GROUP BY DATE(%s) ORDER BY COUNT(*) LIMIT 10",
                                column.name, tableName, column.name));
                result = stmt.executeQuery();
                List<Object> datetimeBottom10 = new ArrayList<Object>();
                while (result.next()) {
                    datetimeBottom10.add(result.getObject(1));
                    datetimeBottom10.add(result.getLong(2));
                }
                datetimeStats.put("bottom10", datetimeBottom10);

            }

            String statsJson = JSONValue.toJSONString(stats);

            PreparedStatement stmtStats = connStats.prepareStatement("UPDATE dp_column SET stats = ? WHERE id = ?");
            stmtStats.setString(1, statsJson);
            stmtStats.setInt(2, column.id);
            if (stmtStats.executeUpdate() == 0) {
                throw new SQLException("Affected rows is zero.");
            }

        } catch (SQLException e) {
            logger.error("Fail to stats column id " + column.id, e);
            return false;
        }

        return true;
    }

    @Override
    public void run() {

        logger.info("Job started.");

        while (!Thread.interrupted()) {

            do {

                connectStats();
                if (connStats == null) {
                    break;
                }

                List<QueueItem> queue = fetchQueue();
                if (queue == null) {
                    break;
                }

                for (QueueItem item : queue) {
                    int status = process(item) ? 2 : 3;
                    updateStatus(item, status);
                }

            } while (false);

            logger.info("Sleeping...");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                break;
            }
        }

        logger.info("Job ended.");

    }

    public static void main(String[] args) throws Exception {
        new App().run();
    }

}
