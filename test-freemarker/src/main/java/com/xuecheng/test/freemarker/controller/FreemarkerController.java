package com.xuecheng.test.freemarker.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RequestMapping("freemarker")
@Controller
public class FreemarkerController {

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("test1")
    public String test1(Model model){
        model.addAttribute("name", "Geligamesh");
        return "test";
    }

    @RequestMapping("banner")
    public String index_banner(Map<String,Object> map) {
        ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31001/cms/config/getmodel/5a791725dd573c3574ee333f", Map.class);
        Map body = forEntity.getBody();
        map.putAll(body);
        System.out.println(body);
        return "index_banner";
    }

    @RequestMapping("course")
    public String course(Map<String,Object> map) {
        ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31200/course/courseview/4028e581617f945f01617f9dabc40000", Map.class);
        Map body = forEntity.getBody();
        map.putAll(body);
        return "course";
    }
}
