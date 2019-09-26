package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CourseBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CourseBaseRepositoryTest {

    @Autowired
    private CourseBaseRepository courseBaseRepository;

    @Test
    public void testFindById() {
        Optional<CourseBase> optional = courseBaseRepository.findById("297e7c7c62b888f00162b8a965510001");
        CourseBase courseBase = optional.get();
        System.out.println(courseBase);
    }
}