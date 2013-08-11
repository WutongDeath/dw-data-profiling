package com.anjuke.dw.data_profiling.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TableController {

    @RequestMapping("/table")
    public String index(ModelMap model) {
        model.addAttribute("name", "Jerry");
        return "table";
    }

}
