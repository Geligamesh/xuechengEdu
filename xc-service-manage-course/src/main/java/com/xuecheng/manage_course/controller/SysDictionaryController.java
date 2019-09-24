package com.xuecheng.manage_course.controller;

import com.xuecheng.api.cms.SysDictinaryControllerApi;
import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_course.service.SysDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("sys/dictionary")
public class SysDictionaryController  implements SysDictinaryControllerApi{

    @Autowired
    private SysDictionaryService sysDictionaryService;

    @Override
    @GetMapping("get/{type}")
    public SysDictionary getByType(@PathVariable("type") String type) {
        return sysDictionaryService.findByDType(type);
    }
}
