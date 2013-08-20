package com.anjuke.dw.data_profiling.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.anjuke.dw.data_profiling.dao.ColumnDao;
import com.anjuke.dw.data_profiling.dao.DatabaseDao;
import com.anjuke.dw.data_profiling.dao.ServerDao;
import com.anjuke.dw.data_profiling.dao.TableDao;
import com.anjuke.dw.data_profiling.form.ServerForm;
import com.anjuke.dw.data_profiling.model.Column;
import com.anjuke.dw.data_profiling.model.Database;
import com.anjuke.dw.data_profiling.model.Server;
import com.anjuke.dw.data_profiling.model.Table;
import com.anjuke.dw.data_profiling.util.ResourceNotFoundException;

@Controller
@RequestMapping("/server")
public class ServerController {

    @Autowired
    private ServerDao serverDao;

    @Autowired
    private DatabaseDao databaseDao;

    @Autowired
    private TableDao tableDao;

    @Autowired
    private ColumnDao columnDao;

    @RequestMapping("/list")
    public String list(ModelMap model) {
        List<Server> serverList = serverDao.findAll();
        model.addAttribute("serverList", serverList);
        return "server/list";
    }

    @RequestMapping(value="/add", method=RequestMethod.GET)
    public String add(ModelMap model) {
        model.addAttribute("serverForm", new ServerForm());
        return "server/add";
    }

    @RequestMapping(value="/add", method=RequestMethod.POST)
    public String addSubmit(@Valid @ModelAttribute("serverForm") ServerForm serverForm,
            BindingResult result, ModelMap model) {

        if (!result.hasErrors()) {

            try {

                if (serverDao.nameExsits(serverForm.getServerName())) {
                    throw new Exception("Server name exsits");
                }

                List<Database> databaseList = new ArrayList<Database>();
                try {

                    Connection conn = DriverManager.getConnection(
                            String.format(
                                    "jdbc:mysql://%s:%d/information_schema?useUnicode=true&characterEncoding=UTF-8",
                                    serverForm.getHost(), serverForm.getPort()),
                            serverForm.getUsername(), serverForm.getPassword());

                    PreparedStatement stmt = conn.prepareStatement(
                            "SELECT table_schema, COUNT(*) AS table_count FROM tables GROUP BY table_schema");
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {

                        String tableSchema = rs.getString("table_schema");
                        if (tableSchema.equals("information_schema")
                                || tableSchema.equals("performance_schema")
                                || tableSchema.equals("mysql")
                                || tableSchema.equals("test")
                                || tableSchema.equals("heartbeat_db")) {
                            continue;
                        }

                        Database database = new Database();
                        database.setName(rs.getString("table_schema"));
                        database.setTableCount(rs.getInt("table_count"));
                        databaseList.add(database);
                    }

                } catch (SQLException e) {
                    throw new Exception("Server not found");
                }

                if (databaseList.size() == 0) {
                    throw new Exception("Server is empty");
                }

                Server server = new Server();
                server.setName(serverForm.getServerName());
                server.setHost(serverForm.getHost());
                server.setPort(serverForm.getPort());
                server.setUsername(serverForm.getUsername());
                server.setPassword(serverForm.getPassword());
                server.setDatabaseCount(databaseList.size());

                Integer serverId = serverDao.insert(server);
                if (serverId == null) {
                    throw new Exception("Fail to add server");
                }

                for (Database database : databaseList) {
                    database.setServerId(serverId);
                    databaseDao.insert(database);
                }

                return "redirect:/server/list";

            } catch (Exception e) {
                result.rejectValue("serverName", null, e.getMessage());
            }

        }

        model.addAttribute("serverForm", serverForm);

        return "server/add";
    }

    @RequestMapping("/delete/{serverId}")
    public String delete(@PathVariable int serverId, ModelMap model) {

        Server server = serverDao.findById(serverId);
        if (server == null) {
            throw new ResourceNotFoundException();
        }

        for (Database database : databaseDao.findByServerId(server.getId())) {

            for (Table table : tableDao.findByDatabaseId(database.getId())) {

                for (Column column : columnDao.findByTableId(table.getId())) {
                    columnDao.delete(column.getId());
                }
                tableDao.delete(table.getId());

            }
            databaseDao.delete(database.getId());

        }
        serverDao.delete(server.getId());

        return "redirect:/server/list";
    }

}
