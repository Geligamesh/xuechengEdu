package com.xuecheng.manage_course.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CoursePic;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private TeachplanRepository teachplanRepository;
    @Autowired
    private CourseBaseRepository courseBaseRepository;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private CoursePicRepository coursePicRepository;

    public TeachplanNode findTeachplanList(String courseId) {
        return teachplanMapper.selectList(courseId);
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseResult addTeachplan(Teachplan teachplan) {
        if (teachplan == null || StringUtils.isEmpty(teachplan.getCourseid()) ||
        StringUtils.isEmpty(teachplan.getPname())) {
            ExceptionCast.cast(CommonCode.INVAILD_PARAM);
        }
        //课程id
        String courseid = teachplan.getCourseid();
        //根节点id
        String parentid = teachplan.getParentid();
        //判断根节点是否为空
        if (StringUtils.isEmpty(parentid)) {
            //如果根节点为空那就默认使用的是课程的根节点id,则需要从数据库查找课程对应的根节点id
            parentid = getTeachplanRoot(courseid);
        }
        //根据parentid查询根节点
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan teachplan1 = optional.get();
        //获得父节点级别
        String parent_grade = teachplan1.getGrade();

        Teachplan teachplanNew = new Teachplan();
        BeanUtils.copyProperties(teachplan, teachplanNew);
        teachplanNew.setParentid(parentid);
        teachplanNew.setStatus("0");

        if (parent_grade.equals("1")) {
            teachplanNew.setGrade("2");
        }else {
            teachplanNew.setGrade("3");
        }

        teachplanRepository.save(teachplanNew);
        return ResponseResult.SUCCESS();
    }

    //查找课程
    private String getTeachplanRoot(String courseId) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()) {
            return null;
        }
        CourseBase courseBase = optional.get();
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        if (teachplanList == null || teachplanList.size() <= 0) {
            //如果数据库中没有跟几点的信息，则创建根节点并将其保存到数据库
            Teachplan teachplan = new Teachplan();
            teachplan.setStatus("0");
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setCourseid(courseId);
            teachplan.setPname(courseBase.getName());
            teachplanRepository.save(teachplan);
            //返回根节点id
            return teachplan.getId();
        }
        return teachplanList.get(0).getId();
    }

    //课程列表分页查询
    public QueryResponseResult findCourseListPage(int page, int size, CourseListRequest courseListRequest) {

        if (courseListRequest == null) {
            courseListRequest = new CourseListRequest();
        }

        if (page <= 0) {
            page = 0;
        }
        if(size <= 0) {
            size = 10;
        }
        //设置分页参数
        PageHelper.startPage(page, size);
        //分页查询
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        //查询列表
        List<CourseInfo> list = courseListPage.getResult();
        //查询总数
        long total = courseListPage.getTotal();
        //查询结果集
        QueryResult<CourseInfo> queryResult = new QueryResult<>();
        queryResult.setList(list);
        queryResult.setTotal(total);
        return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
    }

    //添加课程基本信息
    @Transactional(propagation = Propagation.REQUIRED)
    public AddCourseResult addCourseBase(CourseBase courseBase) {
        courseBase.setStatus("202002");
        courseBaseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS,courseBase.getId());
    }

    //根据课程id获取课程信息
    public CourseBase getCourseBaseById(String courseId) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    //更新课程id更新课程信息
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseResult updateCourseBase(String id,CourseBase courseBase) {
        CourseBase one = this.getCourseBaseById(id);
        if (one == null) {
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
        one.setName(courseBase.getName());
        one.setUsers(courseBase.getUsers());
        one.setMt(courseBase.getMt());
        one.setSt(courseBase.getSt());
        one.setGrade(courseBase.getGrade());
        one.setStudymodel(courseBase.getStudymodel());
        one.setDescription(courseBase.getDescription());
        courseBaseRepository.save(one);
        return ResponseResult.SUCCESS();
    }

    //添加课程id和图片id的关联信息
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseResult addCoursePic(String courseId,String pic) {
        CoursePic coursePic = null;
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if (optional.isPresent()) {
            coursePic = new CoursePic();
        }
        if (coursePic == null) {
            coursePic = new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //查询课程图片
    public CoursePic findCoursePic(String courseId) {
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        return optional.orElse(null);
    }

    //删除课程id和图片id的关联信息
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseResult deleteCoursePic(String courseId) {
        long result = coursePicRepository.deleteByCourseid(courseId);
        if (result > 0) {
            return ResponseResult.SUCCESS();
        }
        return ResponseResult.FAIL();
    }
}
