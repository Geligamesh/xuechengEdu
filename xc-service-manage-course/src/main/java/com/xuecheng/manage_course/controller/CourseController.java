package com.xuecheng.manage_course.controller;

import  com.xuecheng.api.course.CourseControllerApi;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.XcOauth2Util;
import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_course.service.CourseMarketService;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("course")
public class CourseController extends BaseController implements CourseControllerApi{

    @Autowired
    private CourseService courseService;
    @Autowired
    private CourseMarketService courseMarketService;

    //当用户拥有这个course_teachplan_list权限的时候方可访问此方法
    @Override
    // @PreAuthorize(value = "hasAuthority('course_teachplan_list')")
    @GetMapping("teachplan/list/{courseId}")
    public TeachplanNode findTeachplanList(@PathVariable("courseId") String courseId) {
        return courseService.findTeachplanList(courseId);
    }

    @Override
    @PostMapping("teachplan/add")
    // @PreAuthorize(value = "hasAuthority('course_teachplan_add')")
    public ResponseResult addTeachplan(@RequestBody Teachplan teachplan) {
        return courseService.addTeachplan(teachplan);
    }

    @Override
    // @PreAuthorize(value = "hasAuthority('course_find_list')")
    @GetMapping("coursebase/list/{page}/{size}")
    public QueryResponseResult findCourseList(@PathVariable("page") int page,
                                              @PathVariable("size") int size,
                                              CourseListRequest courseListRequest) {

        //获取当前用户的信息
        XcOauth2Util xcOauth2Util = new XcOauth2Util();
        XcOauth2Util.UserJwt userJwt = xcOauth2Util.getUserJwtFromHeader(request);
        if (userJwt == null) {
            ExceptionCast.cast(CommonCode.UNAUTHENTICATED);
        }
        //company == null
        String companyId = userJwt.getCompanyId();
        //当前用户所属单位
        companyId = "1";
        return courseService.findCourseListPage(companyId,page, size,courseListRequest);
    }

    @Override
    @PostMapping("coursebase/add")
    public AddCourseResult addCourseBase(@RequestBody CourseBase courseBase) {
        return courseService.addCourseBase(courseBase);
    }

    @Override
    @GetMapping("coursebase/get/{courseId}")
    public CourseBase getCourseBaseById(@PathVariable("courseId") String courseId) {
        return courseService.getCourseBaseById(courseId);
    }

    @Override
    @PutMapping("coursebase/update/{id}")
    public ResponseResult updateCourseBase(@PathVariable("id") String id, @RequestBody CourseBase courseBase) {
        return courseService.updateCourseBase(id, courseBase);
    }

    @Override
    @GetMapping("coursemarket/get/{courseId}")
    public CourseMarket getCourseMarketById(@PathVariable("courseId") String courseId) {
        return courseMarketService.getCourseMarketById(courseId);
    }

    @Override
    @PutMapping("coursemarket/update/{id}")
    public ResponseResult updateCourseMarket(@PathVariable("id") String id, @RequestBody CourseMarket courseMarket) {
        CourseMarket one = courseMarketService.updateCourseMarket(id, courseMarket);
        if (one != null) {
            return ResponseResult.SUCCESS();
        }
        return ResponseResult.FAIL();
    }

    @Override
    @PostMapping("coursepic/add")
    public ResponseResult addCoursePic(@RequestParam("courseId") String courseId,
                                       @RequestParam("pic") String pic) {
        return courseService.addCoursePic(courseId, pic);
    }

    @Override
    @GetMapping("coursepic/list/{courseId}")
    // @PreAuthorize(value = "hasAuthority('course_find_pic')")
    public CoursePic findCoursePic(@PathVariable("courseId") String courseId) {
        return courseService.findCoursePic(courseId);
    }

    @Override
    @DeleteMapping("coursepic/delete")
    public ResponseResult deleteCoursePic(@RequestParam("courseId") String courseId) {
        return courseService.deleteCoursePic(courseId);
    }

    @Override
    @GetMapping("courseview/{id}")
    public CourseView courseView(@PathVariable("id") String id) {
        return courseService.getCourseView(id);
    }

    @Override
    @PostMapping("preview/{courseId}")
    public CoursePublishResult preview(@PathVariable("courseId") String courseId) {
        return courseService.preview(courseId);
    }

    @Override
    @PostMapping("publish/{courseId}")
    public CoursePublishResult publish(@PathVariable("courseId") String courseId) {
        return courseService.publish(courseId);
    }

    @Override
    @PostMapping("savemedia")
    public ResponseResult savemedia(@RequestBody TeachplanMedia teachplanMedia) {
        return courseService.savemedia(teachplanMedia);
    }
}
