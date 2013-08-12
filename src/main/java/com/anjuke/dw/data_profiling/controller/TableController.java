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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.anjuke.dw.data_profiling.dao.ColumnDao;
import com.anjuke.dw.data_profiling.dao.TableDao;
import com.anjuke.dw.data_profiling.model.Column;
import com.anjuke.dw.data_profiling.model.Table;
import com.anjuke.dw.data_profiling.util.ResourceNotFoundException;

@Controller
@RequestMapping("/table")
public class TableController {

    private static final Map<Integer, String> typeFlagMap;

    static {
        typeFlagMap = new LinkedHashMap<Integer, String>();
        typeFlagMap.put(1, "Numeric");
        typeFlagMap.put(2, "String");
        typeFlagMap.put(4, "Datetime");
    }

    private JSONParser parser = new JSONParser();
    private Logger logger = Logger.getLogger(TableController.class);

    @Autowired
    private TableDao tableDao;

    @Autowired
    private ColumnDao columnDao;

    @RequestMapping("/view/{tableId}")
    public String index(@PathVariable int tableId, ModelMap model) {

        Table table = tableDao.findById(tableId);
        if (table == null) {
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

        model.addAttribute("table", table);
        model.addAttribute("columnList", columnList);
        model.addAttribute("typeFlagMap", typeFlagMap);

        return "table";
    }

}
