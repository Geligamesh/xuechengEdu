package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.system.SysDictionary;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SysDictionaryDaoTest {

    @Autowired
    private SysDictionaryDao sysDictionaryDao;

    @Test
    public void findByType() {
        SysDictionary sysDictionary = sysDictionaryDao.findByDType("200");
        System.out.println(sysDictionary);
    }
}