package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CourseBase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CourseBaseRepository extends JpaRepository<CourseBase,String> {

    //根据课程id查询课程信息
    Optional<CourseBase> findById(String courseId);
}
