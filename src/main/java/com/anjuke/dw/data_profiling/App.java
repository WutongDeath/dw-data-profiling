package com.anjuke.dw.data_profiling;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;

public class App {

    public static void main(String[] args) throws Exception {

        Connection connStats = DriverManager.getConnection("jdbc:mysql://localhost/dwms_db?useUnicode=true&characterEncoding=UTF-8", "dwms_db", "dwms_db");
        PreparedStatement stmtStats = connStats.prepareStatement("UPDATE dp_column SET stats = ? WHERE table_id = 1 AND name = 'toward'");

        Connection connStage = DriverManager.getConnection("jdbc:mysql://10.20.8.39:3306/dw_stage?useUnicode=true&characterEncoding=UTF-8", "readonly_v2", "aNjuKe9dx1Pdw");

        PreparedStatement stmt = connStage.prepareStatement("SELECT MIN(toward), MAX(toward), AVG(toward), STDDEV_POP(toward) FROM st_dw_haozu_prop");
        ResultSet result = stmt.executeQuery();
        result.next();

        Map<String, Object> stats = new HashMap<String, Object>();
        stats.put("min", result.getDouble(1));
        stats.put("max", result.getDouble(2));
        stats.put("avg", result.getDouble(3));
        stats.put("sd", result.getDouble(4));

        stmt.close();
        stmt = connStage.prepareStatement("SELECT toward, COUNT(*) FROM st_dw_haozu_prop GROUP BY toward ORDER BY COUNT(*) DESC LIMIT 10");
        result = stmt.executeQuery();

        List<Object> top10 = new ArrayList<Object>();
        while (result.next()) {
            top10.add(result.getObject(1));
            top10.add(result.getLong(2));
        }
        stats.put("top10", top10);

        String statsString = JSONValue.toJSONString(stats);
        System.out.println(statsString);

        stmtStats.setString(1, statsString);
        int affectedRows = stmtStats.executeUpdate();
        System.out.println("Affected rows: " + String.valueOf(affectedRows));

    }

}
