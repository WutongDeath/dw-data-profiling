package com.anjuke.dw.data_profiling.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import com.anjuke.dw.data_profiling.dao.TableDao;
import com.anjuke.dw.data_profiling.dao.UpdateQueueDao;
import com.anjuke.dw.data_profiling.model.Column;
import com.anjuke.dw.data_profiling.model.Database;
import com.anjuke.dw.data_profiling.model.Table;
import com.anjuke.dw.data_profiling.service.MetaService;
import com.anjuke.dw.data_profiling.util.ResourceNotFoundException;

@Controller
@RequestMapping("/table")
public class TableController {

    private static final Map<Integer, String> typeFlagMap;

    static {
        typeFlagMap = new LinkedHashMap<Integer, String>();
        typeFlagMap.put(1, "Num");
        typeFlagMap.put(2, "Str");
        typeFlagMap.put(4, "Date");
    }

    private JSONParser parser = new JSONParser();
    private Logger logger = Logger.getLogger(TableController.class);

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

    @RequestMapping("/view/{tableId}")
    public String view(@PathVariable int tableId, ModelMap model) {

        Table table = tableDao.findById(tableId);
        if (table == null) {
            throw new ResourceNotFoundException();
        }

        Database database = databaseDao.findById(table.getDatabaseId());
        if (database == null) {
            throw new ResourceNotFoundException();
        }

        List<Map<String, Object>> columnList = new ArrayList<Map<String, Object>>();
        for (Column c : columnDao.findByTableId(tableId)) {

            Map<String, Object> m = new HashMap<String, Object>();

            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("type", c.getType());
            m.put("typeFlag", c.getTypeFlag());

            JSONObject stats = null;
            try {
                stats = (JSONObject) parser.parse(c.getStats());
            } catch (ParseException e) {
                logger.warn("Invalid column stats.", e);
                continue;
            }

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

            columnList.add(m);
        }

        model.addAttribute("database", database);
        model.addAttribute("table", table);
        model.addAttribute("columnList", columnList);
        model.addAttribute("typeFlagMap", typeFlagMap);

        return "table/view";
    }

    @RequestMapping("/column/{columnId}")
    public String column(@PathVariable int columnId, ModelMap model) {

        Column column = columnDao.findById(columnId);
        if (column == null) {
            throw new ResourceNotFoundException();
        }

        Table table = tableDao.findById(column.getTableId());
        if (table == null) {
            throw new ResourceNotFoundException();
        }

        Column c = column;

        JSONObject stats = null;
        try {
            stats = (JSONObject) parser.parse(c.getStats());
        } catch (ParseException e) {
            logger.warn("Invalid column stats.", e);
            return "redirect:/table/view/" + table.getId();
        }

        // general
        Map<String, Object> generalStatsMap = new HashMap<String, Object>();
        model.addAttribute("generalStats", generalStatsMap);

        JSONObject generalStats = (JSONObject) stats.get("general");
        Long nullCount = (Long) generalStats.get("null");
        generalStatsMap.put("nullCount", nullCount);
        generalStatsMap.put("nullPercent", String.format("%.2f", nullCount * 100.0 / table.getRowCount()));
        generalStatsMap.put("distinctValues", (Long) generalStats.get("distinct"));

        if ((c.getTypeFlag() & 1) == 1) { // numeric

            Map<String, Object> numericStatsMap = new HashMap<String, Object>();
            model.addAttribute("numericStats", numericStatsMap);

            JSONObject numericStats = (JSONObject) stats.get("numeric");
            numericStatsMap.put("min", (Double) numericStats.get("min"));
            numericStatsMap.put("max", (Double) numericStats.get("max"));
            numericStatsMap.put("avg", (Double) numericStats.get("avg"));
            numericStatsMap.put("sd", (Double) numericStats.get("sd"));

            JSONArray numericTop10 = (JSONArray) numericStats.get("top10");
            Map<String, Long> numericTop10Map = new LinkedHashMap<String, Long>();
            for (int i = 0; i < numericTop10.size(); i += 2) {
                numericTop10Map.put((String) numericTop10.get(i), (Long) numericTop10.get(i + 1));
            }
            numericStatsMap.put("top10", numericTop10Map);

            JSONArray numericBottom10 = (JSONArray) numericStats.get("bottom10");
            Map<String, Long> numericBottom10Map = new LinkedHashMap<String, Long>();
            for (int i = 0; i < numericBottom10.size(); i += 2) {
                numericBottom10Map.put((String) numericBottom10.get(i), (Long) numericBottom10.get(i + 1));
            }
            numericStatsMap.put("bottom10", numericBottom10Map);
        }

        if ((c.getTypeFlag() & 2) == 2) { // string

            Map<String, Object> stringStatsMap = new HashMap<String, Object>();
            model.addAttribute("stringStats", stringStatsMap);

            JSONObject stringStats = (JSONObject) stats.get("string");
            stringStatsMap.put("minLength", (Long) stringStats.get("min_length"));
            stringStatsMap.put("maxLength", (Long) stringStats.get("max_length"));
            stringStatsMap.put("avgLength", (Long) stringStats.get("avg_length"));

            JSONArray stringTop10 = (JSONArray) stringStats.get("top10");
            Map<String, Long> stringTop10Map = new LinkedHashMap<String, Long>();
            for (int i = 0; i < stringTop10.size(); i += 2) {
                stringTop10Map.put((String) stringTop10.get(i), (Long) stringTop10.get(i + 1));
            }
            stringStatsMap.put("top10", stringTop10Map);
            logger.info(stringTop10Map);

            JSONArray stringBottom10 = (JSONArray) stringStats.get("bottom10");
            Map<String, Long> stringBottom10Map = new LinkedHashMap<String, Long>();
            for (int i = 0; i < stringBottom10.size(); i += 2) {
                stringBottom10Map.put((String) stringBottom10.get(i), (Long) stringBottom10.get(i + 1));
            }
            stringStatsMap.put("bottom10", stringBottom10Map);

        }

        if ((c.getTypeFlag() & 4) == 4) { // datetime

            Map<String, Object> datetimeStatsMap = new HashMap<String, Object>();
            model.addAttribute("datetimeStats", datetimeStatsMap);

            JSONObject datetimeStats = (JSONObject) stats.get("datetime");
            datetimeStatsMap.put("min", (String) datetimeStats.get("min"));
            datetimeStatsMap.put("max", (String) datetimeStats.get("max"));

        }

        model.addAttribute("table", table);
        model.addAttribute("column", column);
        model.addAttribute("typeFlagMap", typeFlagMap);

        return "table/column";
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

        return "table/list";
    }

    @RequestMapping(value="/get_info", produces="application/json")
    @ResponseBody
    public String getInfo(@RequestParam("database_id") int databaseId,
            @RequestParam("tables") String tables) {

        List<String> tableNameList = Arrays.asList(tables.split(","));
        if (tableNameList.size() == 0) {
            return "{}";
        }

        Map<String, Map<String, Object>> tableInfoMap = metaService.getTableInfo(databaseId, tableNameList);
        if (tableInfoMap == null) {
            return "{}";
        }

        return JSONValue.toJSONString(tableInfoMap);

    }

}
