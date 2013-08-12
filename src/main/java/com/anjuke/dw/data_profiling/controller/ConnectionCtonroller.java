package com.anjuke.dw.data_profiling.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.anjuke.dw.data_profiling.dao.ConnectionDao;
import com.anjuke.dw.data_profiling.model.Connection;

@Controller
@RequestMapping("/connection")
public class ConnectionCtonroller {

    @Autowired
    private ConnectionDao connectionDao;

    @RequestMapping("/list")
    public String list(ModelMap model) {

        List<Connection> connectionList = connectionDao.findAll();

        model.addAttribute("connectionList", connectionList);

        return "connection/list";
    }

}
