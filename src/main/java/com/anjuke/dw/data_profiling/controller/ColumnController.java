package com.anjuke.dw.data_profiling.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.anjuke.dw.data_profiling.dao.ColumnDao;
import com.anjuke.dw.data_profiling.dao.DatabaseDao;
import com.anjuke.dw.data_profiling.dao.TableDao;
import com.anjuke.dw.data_profiling.model.Column;
import com.anjuke.dw.data_profiling.model.Database;
import com.anjuke.dw.data_profiling.model.Table;
import com.anjuke.dw.data_profiling.service.MetaService;
import com.anjuke.dw.data_profiling.util.ResourceNotFoundException;

@Controller
@RequestMapping("/column")
public class ColumnController {

    private static Logger logger = Logger.getLogger(ColumnController.class);
    private static final Map<Integer, String> typeFlagMap;

    static {
        typeFlagMap = new LinkedHashMap<Integer, String>();
        typeFlagMap.put(1, "Num");
        typeFlagMap.put(2, "Str");
        typeFlagMap.put(4, "Date");
    }

    @Autowired
    private DatabaseDao databaseDao;

    @Autowired
    private TableDao tableDao;

    @Autowired
    private ColumnDao columnDao;

    @Autowired
    private MetaService metaService;

    private JSONParser parser = new JSONParser();

    @RequestMapping("/list/")
    public String list(@RequestParam("databaseId") int databaseId,
            @RequestParam("table") String tableName, ModelMap model) {

        Database database = databaseDao.findById(databaseId);
        if (database == null) {
            throw new ResourceNotFoundException();
        }

        Table table = tableDao.findByDatabaseIdAndTableName(databaseId, tableName);

        List<Column> columnList;
        if (table == null) { // not profiled

            table = new Table();
            columnList = new ArrayList<Column>();

            metaService.getTableColumnInfo(databaseId, tableName, table, columnList);
            if (columnList.size() == 0) {
                throw new ResourceNotFoundException();
            }

        } else {

            columnList = columnDao.findByTableId(table.getId());

            Map<Integer, Map<String, Object>> columnStats = new HashMap<Integer, Map<String, Object>>();
            for (Column c : columnList) {

                Map<String, Object> m = new HashMap<String, Object>();

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

                columnStats.put(c.getId(), m);
            }

            model.addAttribute("profiled", true);
            model.addAttribute("columnStats", columnStats);
        }

        model.addAttribute("database", database);
        model.addAttribute("table", table);
        model.addAttribute("columnList", columnList);
        model.addAttribute("typeFlagMap", typeFlagMap);

        return "column/list";
    }

}
