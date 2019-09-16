package com.xuecheng.test.freemarker.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("freemarker")
@Controller
public class FreemarkerController {

    @RequestMapping("test1")
    public String test1(Model model){
        model.addAttribute("name", "Geligamesh");
        return "test";
    }
}
