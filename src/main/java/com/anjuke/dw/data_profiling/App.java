package com.anjuke.dw.data_profiling;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;

public class App {

    private static SimpleDateFormat dfDatetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat dfTime = new SimpleDateFormat("HH:mm:ss");

    public static void main(String[] args) throws Exception {

        Connection connStats = DriverManager.getConnection("jdbc:mysql://localhost/dwms_db?useUnicode=true&characterEncoding=UTF-8", "dwms_db", "dwms_db");
        PreparedStatement stmtStats = connStats.prepareStatement("UPDATE dp_column SET stats = ? WHERE table_id = 1 AND name = ?");

        Connection connStage = DriverManager.getConnection("jdbc:mysql://10.20.8.39:3306/dw_stage?useUnicode=true&characterEncoding=UTF-8", "readonly_v2", "aNjuKe9dx1Pdw");
        PreparedStatement stmt;
        ResultSet result;
        String statsJson;

        Map<String, Map<String, Object>> stats = new HashMap<String, Map<String, Object>>();
        Map<String, Object> generalStats = new HashMap<String, Object>();

        // numeric: toward
        stats.clear();
        generalStats.clear();
        stats.put("general", generalStats);
        stmt = connStage.prepareStatement("SELECT COUNT(*) FROM st_dw_haozu_prop WHERE toward IS NULL");
        result = stmt.executeQuery();
        result.next();
        generalStats.put("null", result.getLong(1));

        stmt = connStage.prepareStatement("SELECT COUNT(DISTINCT toward) FROM st_dw_haozu_prop");
        result = stmt.executeQuery();
        result.next();
        generalStats.put("distinct", result.getLong(1));

        Map<String, Object> numericStats = new HashMap<String, Object>();
        stats.put("numeric", numericStats);

        stmt = connStage.prepareStatement("SELECT MIN(toward), MAX(toward), AVG(toward), STDDEV_POP(toward) FROM st_dw_haozu_prop");
        result = stmt.executeQuery();
        result.next();
        numericStats.put("min", result.getDouble(1));
        numericStats.put("max", result.getDouble(2));
        numericStats.put("avg", result.getDouble(3));
        numericStats.put("sd", result.getDouble(4));

        stmt = connStage.prepareStatement("SELECT toward, COUNT(*) FROM st_dw_haozu_prop GROUP BY toward ORDER BY COUNT(*) DESC LIMIT 10");
        result = stmt.executeQuery();
        List<Object> top10 = new ArrayList<Object>();
        while (result.next()) {
            top10.add(result.getObject(1));
            top10.add(result.getLong(2));
        }
        numericStats.put("top10", top10);

        stmt = connStage.prepareStatement("SELECT toward, COUNT(*) FROM st_dw_haozu_prop GROUP BY toward ORDER BY COUNT(*) LIMIT 10");
        result = stmt.executeQuery();
        List<Object> bottom10 = new ArrayList<Object>();
        while (result.next()) {
            bottom10.add(result.getObject(1));
            bottom10.add(result.getLong(2));
        }
        numericStats.put("bottom10", bottom10);

        statsJson = JSONValue.toJSONString(stats);
        System.out.println(statsJson);

        stmtStats.setString(1, statsJson);
        stmtStats.setString(2, "toward");
        stmtStats.executeUpdate();

        // string: comm_name
        stats.clear();
        generalStats.clear();
        stats.put("general", generalStats);
        stmt = connStage.prepareStatement("SELECT COUNT(*) FROM st_dw_haozu_prop WHERE comm_name IS NULL OR TRIM(comm_name) = ''");
        result = stmt.executeQuery();
        result.next();
        generalStats.put("null", result.getLong(1));

        stmt = connStage.prepareStatement("SELECT COUNT(DISTINCT comm_name) FROM st_dw_haozu_prop");
        result = stmt.executeQuery();
        result.next();
        generalStats.put("distinct", result.getLong(1));

        Map<String, Object> stringStats = new HashMap<String, Object>();
        stats.put("string", stringStats);

        stmt = connStage.prepareStatement("SELECT MIN(LENGTH(comm_name)), MAX(LENGTH(comm_name)), AVG(LENGTH(comm_name)) FROM st_dw_haozu_prop");
        result = stmt.executeQuery();
        result.next();
        stringStats.put("min_length", result.getInt(1));
        stringStats.put("max_length", result.getInt(2));
        stringStats.put("avg_length", result.getInt(3));

        statsJson = JSONValue.toJSONString(stats);
        System.out.println(statsJson);

        stmtStats.setString(1, statsJson);
        stmtStats.setString(2, "comm_name");
        stmtStats.executeUpdate();

        // datetime: created_time
        stats.clear();
        generalStats.clear();
        stats.put("general", generalStats);
        stmt = connStage.prepareStatement("SELECT COUNT(*) FROM st_dw_haozu_prop WHERE created_time IS NULL OR created_time = '0000-00-00 00:00:00'");
        result = stmt.executeQuery();
        result.next();
        generalStats.put("null", result.getLong(1));

        stmt = connStage.prepareStatement("SELECT COUNT(DISTINCT created_time) FROM st_dw_haozu_prop");
        result = stmt.executeQuery();
        result.next();
        generalStats.put("distinct", result.getLong(1));

        Map<String, Object> datetimeStats = new HashMap<String, Object>();
        stats.put("datetime", datetimeStats);

        stmt = connStage.prepareStatement("SELECT MIN(created_time), MAX(created_time)," +
                                          "       MIN(DATE(created_time)), MAX(DATE(created_time))," +
                                          "       MIN(TIME(created_time)), MAX(TIME(created_time)) " +
                                          "FROM st_dw_haozu_prop");
        result = stmt.executeQuery();
        result.next();
        datetimeStats.put("min", dfDatetime.format(result.getTimestamp(1)));
        datetimeStats.put("max", dfDatetime.format(result.getTimestamp(2)));
        datetimeStats.put("min_date", dfDate.format(result.getDate(3)));
        datetimeStats.put("max_date", dfDate.format(result.getDate(4)));
        datetimeStats.put("min_time", dfTime.format(result.getTime(5)));
        datetimeStats.put("max_time", dfTime.format(result.getTime(6)));

        stmt = connStage.prepareStatement("SELECT DATE(created_time), COUNT(*) FROM st_dw_haozu_prop GROUP BY DATE(created_time) ORDER BY COUNT(*) DESC LIMIT 10");
        result = stmt.executeQuery();
        List<Object> datetimeTop10 = new ArrayList<Object>();
        while (result.next()) {
            datetimeTop10.add(result.getObject(1));
            datetimeTop10.add(result.getLong(2));
        }
        datetimeStats.put("top10", datetimeTop10);

        statsJson = JSONValue.toJSONString(stats);
        System.out.println(statsJson);

        stmtStats.setString(1,  statsJson);
        stmtStats.setString(2, "created_time");
        stmtStats.executeUpdate();

    }

}
