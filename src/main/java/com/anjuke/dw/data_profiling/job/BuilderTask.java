package com.anjuke.dw.data_profiling.job;

import java.sql.Connection;
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
import org.springframework.beans.factory.annotation.Autowired;

import com.anjuke.dw.data_profiling.dao.ColumnDao;
import com.anjuke.dw.data_profiling.dao.DatabaseDao;
import com.anjuke.dw.data_profiling.dao.ServerDao;
import com.anjuke.dw.data_profiling.dao.TableDao;
import com.anjuke.dw.data_profiling.dao.UpdateQueueDao;
import com.anjuke.dw.data_profiling.model.Column;
import com.anjuke.dw.data_profiling.model.Database;
import com.anjuke.dw.data_profiling.model.Table;
import com.anjuke.dw.data_profiling.model.UpdateQueue;
import com.anjuke.dw.data_profiling.service.MetaService;
import com.anjuke.dw.data_profiling.util.Functions;

public class BuilderTask implements Runnable {

    private static Logger logger = Logger.getLogger(BuilderTask.class);
    private static SimpleDateFormat dfDatetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat dfTime = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    private ServerDao serverDao;
    @Autowired
    private DatabaseDao databaseDao;
    @Autowired
    private TableDao tableDao;
    @Autowired
    private ColumnDao columnDao;
    @Autowired
    private UpdateQueueDao updateQueueDao;
    @Autowired
    private MetaService metaService;

    private UpdateQueue updateQueue;
    private Connection conn;
    private Table table;

    public BuilderTask(UpdateQueue updateQueue) {
        this.updateQueue = updateQueue;
    }

    @Override
    public void run() {

        logger.info(String.format("Processing queue id: %d, table id: %d.", updateQueue.getId(), updateQueue.getTableId()));

        try {

            int databaseId = 0;
            try {
                table = tableDao.findById(updateQueue.getTableId());
                Database database = databaseDao.findById(table.getDatabaseId());
                databaseId = database.getId();
            } catch (NullPointerException e) {
                throw new Exception("Invalid queue item.", e);
            }

            conn = metaService.openConnection(databaseId, false);
            if (conn == null) {
                throw new Exception("Fail to connect database.");
            }

            process();
            updateQueueDao.updateStatus(updateQueue.getId(), UpdateQueue.STATUS_PROCESSED);
            table.setStatus(Table.STATUS_PROCESSED);
            tableDao.update(table);

        } catch (Exception e) {

            logger.error(e, e.getCause());
            updateQueueDao.updateStatus(updateQueue.getId(), UpdateQueue.STATUS_ERROR);
            if (table != null) {
                table.setStatus(Table.STATUS_ERROR);
                tableDao.update(table);
            }

        } finally {
            metaService.closeConnection(conn);
        }

    }

    private void process() throws Exception {

        long rows = 0, dataLength = 0;
        try {

            PreparedStatement stmtTableStatus = conn.prepareStatement("SHOW TABLE STATUS LIKE ?");
            stmtTableStatus.setString(1, table.getName());
            ResultSet rsTableStatus = stmtTableStatus.executeQuery();
            rsTableStatus.next();
            rows = rsTableStatus.getLong("rows");
            dataLength = rsTableStatus.getLong("data_length");

        } catch (SQLException e) {
            throw new Exception("Unable to get table status.", e);
        }

        table.setRowCount(rows);
        table.setDataLength(dataLength);
        if (!tableDao.update(table)) {
            throw new Exception("Fail to update table.");
        }

        if (rows == 0) {
            throw new Exception("Table is empty.");
        }

        List<Column> columnList = columnDao.findByTableId(table.getId());
        if (columnList.size() == 0) {
            throw new Exception("Column list is empty.");
        }

        List<String> errorColumns = new ArrayList<String>();
        int numProcessed = 0;
        for (Column column : columnList) {
            try {
                processColumn(column);
            } catch (Exception e) {
                logger.error("Fail to process column: " + column.getName(), e);
                errorColumns.add(column.getName());
            }
            table.setProgress(++numProcessed * 100 / columnList.size());
            tableDao.update(table);
        }

        if (errorColumns.size() > 0) {
            throw new Exception("Error columns: " + Functions.joinString(errorColumns, ", "));
        }

    }

    private void processColumn(Column column) throws Exception {

        Map<String, Map<String, Object>> stats = new HashMap<String, Map<String, Object>>();
        PreparedStatement stmt;
        ResultSet result;

        // general
        Map<String, Object> generalStats = new HashMap<String, Object>();
        stats.put("general", generalStats);

        stmt = conn.prepareStatement(String.format("SELECT COUNT(*) FROM %s WHERE %s IS NULL", table.getName(), column.getName()));
        result = stmt.executeQuery();
        result.next();
        generalStats.put("null", result.getLong(1));

        stmt = conn.prepareStatement(String.format("SELECT COUNT(DISTINCT %s) FROM %s", column.getName(), table.getName()));
        result = stmt.executeQuery();
        result.next();
        generalStats.put("distinct", result.getLong(1));

        // numeric
        if ((column.getTypeFlag() & 1) == 1) {

            Map<String, Object> numericStats = new HashMap<String, Object>();
            stats.put("numeric", numericStats);

            stmt = conn.prepareStatement(
                    String.format("SELECT MIN(%s), MAX(%s), AVG(%s), STDDEV_POP(%s) FROM %s",
                            column.getName(), column.getName(), column.getName(), column.getName(), table.getName()));
            result = stmt.executeQuery();
            result.next();
            numericStats.put("min", result.getDouble(1));
            numericStats.put("max", result.getDouble(2));
            numericStats.put("avg", result.getDouble(3));
            numericStats.put("sd", result.getDouble(4));

            stmt = conn.prepareStatement(
                    String.format("SELECT %s, COUNT(*) FROM %s GROUP BY %s ORDER BY COUNT(*) DESC LIMIT 10",
                            column.getName(), table.getName(), column.getName()));
            result = stmt.executeQuery();
            List<Object> numericTop10 = new ArrayList<Object>();
            while (result.next()) {
                numericTop10.add(result.getString(1));
                numericTop10.add(result.getLong(2));
            }
            numericStats.put("top10", numericTop10);

            stmt = conn.prepareStatement(
                    String.format("SELECT %s, COUNT(*) FROM %s GROUP BY %s ORDER BY COUNT(*) LIMIT 10",
                            column.getName(), table.getName(), column.getName()));
            result = stmt.executeQuery();
            List<Object> numericBottom10 = new ArrayList<Object>();
            while (result.next()) {
                numericBottom10.add(result.getString(1));
                numericBottom10.add(result.getLong(2));
            }
            numericStats.put("bottom10", numericBottom10);

        }

        // string
        if ((column.getTypeFlag() & 2) == 2) {

            Map<String, Object> stringStats = new HashMap<String, Object>();
            stats.put("string", stringStats);

            stmt = conn.prepareStatement(
                    String.format("SELECT MIN(LENGTH(%s)), MAX(LENGTH(%s)), AVG(LENGTH(%s)) FROM %s",
                            column.getName(), column.getName(), column.getName(), table.getName()));
            result = stmt.executeQuery();
            result.next();
            stringStats.put("min_length", result.getInt(1));
            stringStats.put("max_length", result.getInt(2));
            stringStats.put("avg_length", result.getInt(3));

            stmt = conn.prepareStatement(
                    String.format("SELECT %s, COUNT(*) FROM %s GROUP BY %s ORDER BY COUNT(*) DESC LIMIT 10",
                            column.getName(), table.getName(), column.getName()));
            result = stmt.executeQuery();
            List<Object> stringTop10 = new ArrayList<Object>();
            while (result.next()) {
                stringTop10.add(result.getString(1));
                stringTop10.add(result.getLong(2));
            }
            stringStats.put("top10", stringTop10);

            stmt = conn.prepareStatement(
                    String.format("SELECT %s, COUNT(*) FROM %s GROUP BY %s ORDER BY COUNT(*) LIMIT 10",
                            column.getName(), table.getName(), column.getName()));
            result = stmt.executeQuery();
            List<Object> stringBottom10 = new ArrayList<Object>();
            while (result.next()) {
                stringBottom10.add(result.getString(1));
                stringBottom10.add(result.getLong(2));
            }
            stringStats.put("bottom10", stringBottom10);

        }

        // datetime
        if ((column.getTypeFlag() & 4) == 4) {

            Map<String, Object> datetimeStats = new HashMap<String, Object>();
            stats.put("datetime", datetimeStats);

            stmt = conn.prepareStatement(
                    String.format("SELECT MIN(%s), MAX(%s), MIN(DATE(%s)), MAX(DATE(%s)), MIN(TIME(%s)), MAX(TIME(%s)) FROM %s WHERE %s != '0000-00-00 00:00:00'",
                            column.getName(), column.getName(), column.getName(), column.getName(), column.getName(), column.getName(), table.getName(), column.getName()));
            result = stmt.executeQuery();
            result.next();
            datetimeStats.put("min", dfDatetime.format(result.getTimestamp(1)));
            datetimeStats.put("max", dfDatetime.format(result.getTimestamp(2)));
            datetimeStats.put("min_date", dfDate.format(result.getDate(3)));
            datetimeStats.put("max_date", dfDate.format(result.getDate(4)));
            datetimeStats.put("min_time", dfTime.format(result.getTime(5)));
            datetimeStats.put("max_time", dfTime.format(result.getTime(6)));

            stmt = conn.prepareStatement(
                    String.format("SELECT DATE(%s), COUNT(*) FROM %s GROUP BY DATE(%s) ORDER BY COUNT(*) DESC LIMIT 10",
                            column.getName(), table.getName(), column.getName()));
            result = stmt.executeQuery();
            List<Object> datetimeTop10 = new ArrayList<Object>();
            while (result.next()) {
                datetimeTop10.add(result.getString(1));
                datetimeTop10.add(result.getLong(2));
            }
            datetimeStats.put("top10", datetimeTop10);

            stmt = conn.prepareStatement(
                    String.format("SELECT DATE(%s), COUNT(*) FROM %s GROUP BY DATE(%s) ORDER BY COUNT(*) LIMIT 10",
                            column.getName(), table.getName(), column.getName()));
            result = stmt.executeQuery();
            List<Object> datetimeBottom10 = new ArrayList<Object>();
            while (result.next()) {
                datetimeBottom10.add(result.getString(1));
                datetimeBottom10.add(result.getLong(2));
            }
            datetimeStats.put("bottom10", datetimeBottom10);

        }

        column.setStats(JSONValue.toJSONString(stats));
        if (!columnDao.update(column)) {
            throw new Exception("Fail to update column.");
        }

    }

}
