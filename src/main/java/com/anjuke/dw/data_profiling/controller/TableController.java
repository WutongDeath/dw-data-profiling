package com.anjuke.dw.data_profiling.controller;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.anjuke.dw.data_profiling.dao.ColumnDao;
import com.anjuke.dw.data_profiling.dao.ConnectionDao;
import com.anjuke.dw.data_profiling.dao.TableDao;
import com.anjuke.dw.data_profiling.dao.UpdateQueueDao;
import com.anjuke.dw.data_profiling.form.TableForm;
import com.anjuke.dw.data_profiling.model.Column;
import com.anjuke.dw.data_profiling.model.Connection;
import com.anjuke.dw.data_profiling.model.Table;
import com.anjuke.dw.data_profiling.model.UpdateQueue;
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
    private ConnectionDao connectionDao;

    @Autowired
    private TableDao tableDao;

    @Autowired
    private ColumnDao columnDao;

    @Autowired
    private UpdateQueueDao updateQueueDao;

    @RequestMapping("/view/{tableId}")
    public String view(@PathVariable int tableId, ModelMap model) {

        Table table = tableDao.findById(tableId);
        if (table == null) {
            throw new ResourceNotFoundException();
        }

        Connection connection = connectionDao.findById(table.getConnectionId());
        if (connection == null) {
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

        model.addAttribute("connection", connection);
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

    @RequestMapping("/list/{connectionId}")
    public String list(@PathVariable int connectionId, ModelMap model) {

        Connection connection = connectionDao.findById(connectionId);
        if (connection == null) {
            throw new ResourceNotFoundException();
        }

        List<Table> tableList = tableDao.findByConnectionId(connectionId);

        model.addAttribute("connection", connection);
        model.addAttribute("tableList", tableList);

        return "table/list";
    }

    @RequestMapping(value="/add/{connectionId}", method=RequestMethod.GET)
    public String add(@PathVariable int connectionId, ModelMap model) {

        Connection connection = connectionDao.findById(connectionId);
        if (connection == null) {
            throw new ResourceNotFoundException();
        }

        model.addAttribute("connection", connection);
        model.addAttribute("tableForm", new TableForm());

        return "table/add";
    }

    private static Pattern ptrnNumericType = Pattern.compile("(?i)(int|decimal|float|double)");
    private static Pattern ptrnStringType = Pattern.compile("(?i)(char|text)");
    private static Pattern ptrnDatetimeType = Pattern.compile("(?i)(date|time)");

    @RequestMapping(value="/add/{connectionId}", method=RequestMethod.POST)
    public String addSubmit(@PathVariable int connectionId,
            @Valid @ModelAttribute("tableForm") TableForm tableForm,
            BindingResult result, ModelMap model) {

        Connection connection = connectionDao.findById(connectionId);
        if (connection == null) {
            throw new ResourceNotFoundException();
        }

        if (!result.hasErrors()) {

            try {

                if (tableDao.nameExists(tableForm.getTableName())) {
                    throw new Exception("table name exists");
                }

                java.sql.Connection conn = DriverManager.getConnection(
                        String.format("jdbc:mysql://%s:%d/%s",
                                connection.getHost(), connection.getPort(), connection.getDatabase()),
                        connection.getUsername(), connection.getPassword());

                PreparedStatement stmt = conn.prepareStatement("DESC " + tableForm.getTableName());
                ResultSet rs = stmt.executeQuery();

                List<Column> columnList = new ArrayList<Column>();
                while (rs.next()) {
                    Column column = new Column();
                    column.setName(rs.getString("field"));
                    column.setType(rs.getString("type"));

                    if (ptrnNumericType.matcher(column.getType()).find()) {
                        column.setTypeFlag(1);
                    } else if (ptrnStringType.matcher(column.getType()).find()) {
                        column.setTypeFlag(2);
                    } else if (ptrnDatetimeType.matcher(column.getType()).find()) {
                        column.setTypeFlag(4);
                    } else {
                        column.setTypeFlag(0);
                    }

                    column.setStats("");
                    columnList.add(column);
                }

                Table table = new Table();
                table.setConnectionId(connection.getId());
                table.setName(tableForm.getTableName());
                table.setStatus(0);
                table.setColumnCount(columnList.size());
                table.setRowCount(0L);
                table.setDataLength(0L);

                Integer tableId = tableDao.insert(table);
                if (tableId == null) {
                    throw new Exception("fail to insert table");
                }

                for (Column column : columnList) {
                    column.setTableId(tableId);
                    columnDao.insert(column);
                }

                UpdateQueue updateQueue = new UpdateQueue();
                updateQueue.setTableId(tableId);
                updateQueue.setStatus(1);
                updateQueueDao.insert(updateQueue);

                return "redirect:/table/view/" + tableId;

            } catch (SQLException e) {
                result.rejectValue("tableName", null, "table not found");
            } catch (Exception e) {
                result.rejectValue("tableName", null, e.getMessage());
            }

        }

        model.addAttribute("connection", connection);

        return "table/add";
    }

}
