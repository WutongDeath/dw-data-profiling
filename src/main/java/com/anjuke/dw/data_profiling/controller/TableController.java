package com.anjuke.dw.data_profiling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.anjuke.dw.data_profiling.dao.TableDao;

@Controller
@RequestMapping("/table")
public class TableController {

    @Autowired
    private TableDao tableDao;

    @RequestMapping("/view/{tableId}")
    public String index(@PathVariable int tableId, Model model) {
        model.addAttribute("name", tableDao.findById(1).getName());
        return "table";
    }

}
