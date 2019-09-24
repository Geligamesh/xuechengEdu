package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.Teachplan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeachplanRepository extends JpaRepository<Teachplan,String> {

    //根据课程和parentid查询teachplan
    List<Teachplan> findByCourseidAndParentid(String courseId,String parentId);
}
