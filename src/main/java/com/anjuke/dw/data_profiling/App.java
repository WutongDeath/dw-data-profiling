package com.anjuke.dw.data_profiling;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONValue;

public class App {

    public static void main(String[] args) throws Exception {

//        Connection connStats = DriverManager.getConnection("jdbc:mysql://localhost/dwms_db?useUnicode=true&characterEncoding=UTF-8", "dwms_db", "dwms_db");
        Connection connStage = DriverManager.getConnection("jdbc:mysql://10.20.8.39:3306/dw_stage?useUnicode=true&characterEncoding=UTF-8", "readonly_v2", "aNjuKe9dx1Pdw");

        PreparedStatement stmt = connStage.prepareStatement("SELECT MIN(comm_id), MAX(comm_id), AVG(comm_id), STDDEV_POP(comm_id) FROM st_dw_haozu_prop");
        ResultSet result = stmt.executeQuery();
        result.next();

        Map<String, Object> stats = new HashMap<String, Object>();
        stats.put("min", result.getDouble(1));
        stats.put("max", result.getDouble(2));
        stats.put("avg", result.getDouble(3));
        stats.put("sd", result.getDouble(4));

        String statsString = JSONValue.toJSONString(stats);
        System.out.println(statsString);

    }

}
