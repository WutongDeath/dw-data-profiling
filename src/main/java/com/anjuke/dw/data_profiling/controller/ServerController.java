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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.anjuke.dw.data_profiling.dao.DatabaseDao;
import com.anjuke.dw.data_profiling.dao.ServerDao;
import com.anjuke.dw.data_profiling.form.ServerForm;
import com.anjuke.dw.data_profiling.model.Database;
import com.anjuke.dw.data_profiling.model.Server;

@Controller
@RequestMapping("/server")
public class ServerController {

    @Autowired
    private ServerDao serverDao;

    @Autowired
    private DatabaseDao databaseDao;

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
                                    "jdbc:mysql://%s:%d/?useUnicode=true&characterEncoding=UTF-8",
                                    serverForm.getHost(), serverForm.getPort()),
                            serverForm.getUsername(), serverForm.getPassword());

                    PreparedStatement stmt = conn.prepareStatement("SELECT schema_name FROM information_schema.schemata");
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        Database database = new Database();
                        database.setName(rs.getString("schema_name"));
                        database.setTableCount(0);
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

}
