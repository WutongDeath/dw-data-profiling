package com.anjuke.dw.data_profiling.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.anjuke.dw.data_profiling.dao.DatabaseDao;
import com.anjuke.dw.data_profiling.dao.ServerDao;
import com.anjuke.dw.data_profiling.model.Database;
import com.anjuke.dw.data_profiling.model.Server;
import com.anjuke.dw.data_profiling.service.CommonService;
import com.anjuke.dw.data_profiling.util.ResourceNotFoundException;

@Controller
@RequestMapping("/database")
public class DatabaseController {

    @Autowired
    private ServerDao serverDao;

    @Autowired
    private DatabaseDao databaseDao;

    @Autowired
    private CommonService commonService;

    @RequestMapping("/list/{serverId}")
    public String list(@PathVariable int serverId, ModelMap model) {

        Server server = serverDao.findById(serverId);
        if (server == null) {
            throw new ResourceNotFoundException();
        }

        List<Database> databaseList = databaseDao.findByServerId(serverId);

        model.addAttribute("server", server);
        model.addAttribute("databaseList", databaseList);
        model.addAttribute("navi", commonService.getNavi(serverId, null));

        return "database/list";
    }

}
