package com.anjuke.dw.data_profiling.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.anjuke.dw.data_profiling.dao.ColumnDao;
import com.anjuke.dw.data_profiling.dao.DatabaseDao;
import com.anjuke.dw.data_profiling.dao.ServerDao;
import com.anjuke.dw.data_profiling.dao.TableDao;
import com.anjuke.dw.data_profiling.dao.UpdateQueueDao;
import com.anjuke.dw.data_profiling.model.Column;
import com.anjuke.dw.data_profiling.model.Database;
import com.anjuke.dw.data_profiling.model.Server;
import com.anjuke.dw.data_profiling.model.Table;
import com.anjuke.dw.data_profiling.service.CommonService;
import com.anjuke.dw.data_profiling.service.MetaService;
import com.anjuke.dw.data_profiling.util.Functions;
import com.anjuke.dw.data_profiling.util.ResourceNotFoundException;

@Controller
@RequestMapping("/table")
public class TableController {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(TableController.class);
    private static final Map<Integer, String> typeFlagMap;

    static {
        typeFlagMap = new LinkedHashMap<Integer, String>();
        typeFlagMap.put(1, "Numeric");
        typeFlagMap.put(2, "String");
        typeFlagMap.put(4, "Datetime");
    }

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
    @Autowired
    private CommonService commonService;

    private JSONParser parser = new JSONParser();
    private SimpleDateFormat dfDatetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @RequestMapping(value="/get_column_details", produces="application/json; charset=utf-8")
    @ResponseBody
    public String column(@RequestParam("columnId") int columnId) {

        Column column = columnDao.findById(columnId);
        if (column == null) {
            return "{}";
        }

        Table table = tableDao.findById(column.getTableId());
        if (table == null) {
            return "{}";
        }

        Column c = column;

        JSONObject stats = null;
        try {
            stats = (JSONObject) parser.parse(c.getStats());
        } catch (ParseException e) {
            return "{}";
        }

        Map<String, Object> model = new HashMap<String, Object>();

        model.put("columnId", c.getId());
        model.put("columnName", c.getName());
        model.put("columnType", c.getType());
        model.put("typeFlag", c.getTypeFlag());
        model.put("comment", c.getComment());
        model.put("updated", c.getUpdated() == null ? "-" : dfDatetime.format(c.getUpdated()));

        List<String> typeFlagString = new ArrayList<String>();
        for (Entry<Integer, String> typeFlagEntry : typeFlagMap.entrySet()) {
            if ((c.getTypeFlag() & typeFlagEntry.getKey()) == typeFlagEntry.getKey()) {
                typeFlagString.add(typeFlagEntry.getValue());
            }
        }
        model.put("typeFlagString", Functions.joinString(typeFlagString, ", "));

        try {

            // general
            Map<String, Object> generalStatsMap = new HashMap<String, Object>();
            model.put("generalStats", generalStatsMap);

            JSONObject generalStats = (JSONObject) stats.get("general");
            Long nullCount = (Long) generalStats.get("null");
            generalStatsMap.put("nullCount", nullCount);
            generalStatsMap.put("nullPercent", String.format("%.2f", nullCount * 100.0 / table.getRowCount()));
            generalStatsMap.put("distinctValues", (Long) generalStats.get("distinct"));

            if ((c.getTypeFlag() & 1) == 1) { // numeric

                Map<String, Object> numericStatsMap = new HashMap<String, Object>();
                model.put("numericStats", numericStatsMap);

                JSONObject numericStats = (JSONObject) stats.get("numeric");
                numericStatsMap.put("min", (Double) numericStats.get("min"));
                numericStatsMap.put("max", (Double) numericStats.get("max"));
                numericStatsMap.put("avg", (Double) numericStats.get("avg"));
                numericStatsMap.put("sd", (Double) numericStats.get("sd"));
                numericStatsMap.put("top10", (JSONArray) numericStats.get("top10"));
                numericStatsMap.put("bottom10", (JSONArray) numericStats.get("bottom10"));
            }

            if ((c.getTypeFlag() & 2) == 2) { // string

                Map<String, Object> stringStatsMap = new HashMap<String, Object>();
                model.put("stringStats", stringStatsMap);

                JSONObject stringStats = (JSONObject) stats.get("string");
                stringStatsMap.put("minLength", (Long) stringStats.get("min_length"));
                stringStatsMap.put("maxLength", (Long) stringStats.get("max_length"));
                stringStatsMap.put("avgLength", (Long) stringStats.get("avg_length"));
                stringStatsMap.put("top10", (JSONArray) stringStats.get("top10"));
                stringStatsMap.put("bottom10", (JSONArray) stringStats.get("bottom10"));

            }

            if ((c.getTypeFlag() & 4) == 4) { // datetime

                Map<String, Object> datetimeStatsMap = new HashMap<String, Object>();
                model.put("datetimeStats", datetimeStatsMap);

                JSONObject datetimeStats = (JSONObject) stats.get("datetime");
                datetimeStatsMap.put("min", (String) datetimeStats.get("min"));
                datetimeStatsMap.put("max", (String) datetimeStats.get("max"));
                datetimeStatsMap.put("minDate", (String) datetimeStats.get("min_date"));
                datetimeStatsMap.put("maxDate", (String) datetimeStats.get("max_date"));
                datetimeStatsMap.put("minTime", (String) datetimeStats.get("max_time"));
                datetimeStatsMap.put("maxTime", (String) datetimeStats.get("max_time"));
                datetimeStatsMap.put("top10", (JSONArray) datetimeStats.get("top10"));
                datetimeStatsMap.put("bottom10", (JSONArray) datetimeStats.get("bottom10"));

            }

        } catch (Exception e) {
            return "{}";
        }

        return JSONValue.toJSONString(model);
    }

    @RequestMapping("/list/{databaseId}")
    public String list(@PathVariable int databaseId, ModelMap model) {

        Database database = databaseDao.findById(databaseId);
        if (database == null) {
            throw new ResourceNotFoundException();
        }

        List<String> tableNameList = metaService.getTableNames(database.getId());
        if (tableNameList == null) {
            tableNameList = new ArrayList<String>();
        }

        model.addAttribute("database", database);
        model.addAttribute("tableNameList", JSONValue.toJSONString(tableNameList));
        model.addAttribute("navi", commonService.getNavi(null, databaseId));

        return "table/list";
    }

    @RequestMapping(value="/get_info/", produces="application/json; charset=utf-8")
    @ResponseBody
    public String getInfo(@RequestParam("databaseId") int databaseId,
            @RequestParam("table") String tableName) {

        Database database = databaseDao.findById(databaseId);
        if (database == null) {
            return "{}";
        }

        Server server = serverDao.findById(database.getServerId());
        if (server == null) {
            return "{}";
        }

        Table table = tableDao.findByDatabaseIdAndTableName(databaseId, tableName);
        List<Column> columnList;

        if (table == null) {

            table = new Table();
            columnList = new ArrayList<Column>();
            metaService.getTableColumnInfo(databaseId, tableName, table, columnList);
            if (table.getName() == null) {
                return "{}";
            }

        } else {
            columnList = columnDao.findByTableId(table.getId());
        }

        if (columnList.size() == 0) {
            return "{}";
        }

        Map<String, Object> tableInfo = new HashMap<String, Object>();
        tableInfo.put("serverName", server.getName());
        tableInfo.put("databaseName", database.getName());
        tableInfo.put("tableName", table.getName());
        tableInfo.put("columnCount", table.getColumnCount());
        tableInfo.put("rowCount", table.getRowCount());
        tableInfo.put("dataLength", table.getDataLength());
        tableInfo.put("status", table.getStatus());
        tableInfo.put("progress", table.getProgress());
        tableInfo.put("updated", table.getUpdated() == null ? "-" : dfDatetime.format(table.getUpdated()));

        List<Map<String, Object>> columnArray = new ArrayList<Map<String, Object>>();
        int index = 1;
        for (Column c : columnList) {

            Map<String, Object> m = new HashMap<String, Object>();
            columnArray.add(m);

            m.put("columnId", c.getId());
            m.put("columnIndex", index++);
            m.put("columnName", c.getName());
            m.put("columnType", c.getType());
            m.put("typeFlag", c.getTypeFlag());
            m.put("comment", c.getComment());
            m.put("updated", c.getUpdated() == null ? "-" : dfDatetime.format(c.getUpdated()));

            List<String> typeFlagString = new ArrayList<String>();
            for (Entry<Integer, String> typeFlagEntry : typeFlagMap.entrySet()) {
                if ((c.getTypeFlag() & typeFlagEntry.getKey()) == typeFlagEntry.getKey()) {
                    typeFlagString.add(typeFlagEntry.getValue());
                }
            }
            m.put("typeFlagString", Functions.joinString(typeFlagString, ", "));

            // default
            m.put("nullCount", "-");
            m.put("nullPercent", "-");
            m.put("distinctValues", "-");
            m.put("min", "-");
            m.put("max", "-");
            m.put("avg", "-");
            m.put("sd", "-");
            m.put("hasDetails", false);

            // stats
            JSONObject stats = null;
            if (c.getStats() != null && c.getStats().length() > 0) {
                try {
                    stats = (JSONObject) parser.parse(c.getStats());
                } catch (ParseException e) {}
            }

            if (stats == null) {
                continue;
            }

            try {

                // general
                JSONObject generalStats = (JSONObject) stats.get("general");
                Long nullCount = (Long) generalStats.get("null");
                m.put("nullCount", nullCount);
                m.put("nullPercent", String.format("%.2f", nullCount * 100.0 / table.getRowCount()));
                m.put("distinctValues", (Long) generalStats.get("distinct"));

                if ((c.getTypeFlag() & 1) == 1) { // numeric

                    JSONObject numericStats = (JSONObject) stats.get("numeric");
                    m.put("min", (Double) numericStats.get("min"));
                    m.put("max", (Double) numericStats.get("max"));
                    m.put("avg", (Double) numericStats.get("avg"));
                    m.put("sd", (Double) numericStats.get("sd"));

                } else if ((c.getTypeFlag() & 2) == 2) { // string

                    JSONObject stringStats = (JSONObject) stats.get("string");
                    m.put("min", (Long) stringStats.get("min_length"));
                    m.put("max", (Long) stringStats.get("max_length"));
                    m.put("avg", (Long) stringStats.get("avg_length"));
                    m.put("sd", "-");

                } else if ((c.getTypeFlag() & 4) == 4) { // datetime

                    JSONObject datetimeStats = (JSONObject) stats.get("datetime");
                    m.put("min", (String) datetimeStats.get("min"));
                    m.put("max", (String) datetimeStats.get("max"));
                    m.put("avg", "-");
                    m.put("sd", "-");

                }

                m.put("hasDetails", true);

            } catch (NullPointerException e) {}

        }

        tableInfo.put("columnList", columnArray);

        return JSONValue.toJSONString(tableInfo);
    }

    @RequestMapping(value="/start_profiling/", produces="application/json; charset=utf-8")
    @ResponseBody
    public String startProfiling(@RequestParam("databaseId") int databaseId,
            @RequestParam("table") String tableName) {

        Table table = tableDao.findByDatabaseIdAndTableName(databaseId, tableName);
        if (table != null && table.getStatus() == Table.STATUS_NEW) {
            return Functions.output("error", "duplicate request");
        }

        Table tableNew = new Table();
        List<Column> columnListNew = new ArrayList<Column>();
        metaService.getTableColumnInfo(databaseId, tableName, tableNew, columnListNew);
        if (columnListNew.size() == 0) {

            if (table != null) { // delete
                for (Column column : columnDao.findByTableId(table.getId())) {
                    columnDao.delete(column.getId());
                }
                tableDao.delete(table.getId());
            }

            return Functions.output("error", "table not found");
        }

        if (table == null) { // new table

            tableNew.setStatus(Table.STATUS_NEW);
            Integer tableId = tableDao.insert(tableNew);
            if (tableId == null) {
                return Functions.output("error", "fail to insert table");
            }

            for (Column column : columnListNew) {
                column.setTableId(tableId);
                columnDao.insert(column);
            }

            updateQueueDao.insert(tableId);

        } else { // update

            table.setColumnCount(tableNew.getColumnCount());
            table.setRowCount(tableNew.getRowCount());
            table.setDataLength(tableNew.getDataLength());
            table.setStatus(Table.STATUS_NEW);
            table.setProgress(0);
            if (!tableDao.update(table)) {
                return Functions.output("error", "fail to update table");
            }

            Map<String, Column> columnMapNew = new HashMap<String, Column>();
            for (Column columnNew : columnListNew) {
                columnMapNew.put(columnNew.getName(), columnNew);
            }

            for (Column column : columnDao.findByTableId(table.getId())) {
                Column columnNew = columnMapNew.get(column.getName());
                if (columnNew == null) { // deleted
                    columnDao.delete(column.getId());
                } else {

                    if (!column.getType().equals(columnNew.getType())) { // updated
                        column.setType(columnNew.getType());
                        column.setTypeFlag(columnNew.getTypeFlag());
                        column.setStats("");
                        columnDao.update(column);
                    }

                    columnMapNew.remove(column.getName());
                }
            }

            for (Column columnNew : columnMapNew.values()) { // new
                columnNew.setTableId(table.getId());
                columnDao.insert(columnNew);
            }

            updateQueueDao.insert(table.getId());
        }

        return Functions.output("ok");
    }

}
