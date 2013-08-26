package com.anjuke.dw.data_profiling.controller;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.anjuke.dw.data_profiling.dao.ColumnDao;
import com.anjuke.dw.data_profiling.dao.DatabaseDao;
import com.anjuke.dw.data_profiling.dao.ServerDao;
import com.anjuke.dw.data_profiling.dao.TableDao;
import com.anjuke.dw.data_profiling.form.ServerForm;
import com.anjuke.dw.data_profiling.model.Column;
import com.anjuke.dw.data_profiling.model.Database;
import com.anjuke.dw.data_profiling.model.Server;
import com.anjuke.dw.data_profiling.model.Table;
import com.anjuke.dw.data_profiling.service.CommonService;
import com.anjuke.dw.data_profiling.service.MetaService;
import com.anjuke.dw.data_profiling.util.Functions;
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
    @Autowired
    private CommonService commonService;
    @Autowired
    private MetaService metaService;

    @RequestMapping("/list")
    public String list(ModelMap model) {
        List<Server> serverList = serverDao.findAll();
        model.addAttribute("serverList", serverList);
        model.addAttribute("navi", commonService.getNavi(null, null));
        return "server/list";
    }

    @RequestMapping(value="/add", method=RequestMethod.POST, produces="application/json; charset=utf-8")
    @ResponseBody
    public String addSubmit(@Valid @ModelAttribute("serverForm") ServerForm serverForm,
            BindingResult result, ModelMap model) {

        if (result.hasErrors()) {
            List<String> errors = new ArrayList<String>();
            for (ObjectError error : result.getAllErrors()) {
                errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
            }
            return Functions.output("error", Functions.joinString(errors, "; "));
        }

        try {

            if (serverDao.nameExsits(serverForm.getServerName())) {
                throw new Exception("Server name exsits");
            }

            Server server = new Server();
            server.setName(serverForm.getServerName());
            server.setHost(serverForm.getHost());
            server.setPort(serverForm.getPort());
            server.setUsername(serverForm.getUsername());
            server.setPassword(serverForm.getPassword());

            List<Database> databaseList = metaService.getDatabaseList(server);

            if (databaseList.size() == 0) {
                throw new Exception("Server is empty");
            }

            server.setDatabaseCount(databaseList.size());
            Integer serverId = serverDao.insert(server);
            if (serverId == null) {
                throw new Exception("Fail to add server");
            }

            for (Database database : databaseList) {
                database.setServerId(serverId);
                databaseDao.insert(database);
            }

            return Functions.output("ok");

        } catch (Exception e) {
            return Functions.output("error", e.getMessage());
        }
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
