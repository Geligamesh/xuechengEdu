package com.xuecheng.framework.domain.course.ext;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.CoursePic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CourseView implements Serializable {

    //基础信息
    private CourseBase courseBase;
    //课程图片
    private CoursePic coursePic;
    // 课程营销
    private CourseMarket courseMarket;
    //教学计划
    private TeachplanNode teachplanNode;
}
