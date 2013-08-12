package com.anjuke.dw.data_profiling.controller;

import java.util.List;

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

        List<Column> columnList = columnDao.findByTableId(tableId);
        if (columnList.size() == 0) {
            throw new ResourceNotFoundException();
        }

        model.addAttribute("table", table);
        model.addAttribute("columnList", columnList);

        return "table";
    }

}
