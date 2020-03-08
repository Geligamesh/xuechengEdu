package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    @Autowired
    private CourseMarketRepository courseMarketRepository;
    @Autowired
    private CmsPageClient cmsPageClient;
    @Autowired
    private TeachplanMediaRepository teachplanMediaRepository;
    @Autowired
    private TeachplanMediaPubRepository teachplanMediaPubRepository;
    @Autowired
    private CoursePubRepository coursePubRepository;
    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;

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
    public QueryResponseResult findCourseListPage(String companyId,int page, int size, CourseListRequest courseListRequest) {

        if (courseListRequest == null) {
            courseListRequest = new CourseListRequest();
        }
        //将公司id参数传入dao
        courseListRequest.setCompanyId(companyId);
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
        return new QueryResponseResult<CourseInfo>(CommonCode.SUCCESS, queryResult);
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

    //查询课程的视图，包括基本信息，包括图片，包括营销信息，包括课程计划
    public CourseView getCourseView(String id) {
        CourseView courseView = new CourseView();
        //查询课程的基本信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if (courseBaseOptional.isPresent()) {
            CourseBase courseBase = courseBaseOptional.get();
            courseView.setCourseBase(courseBase);
        }
        //查询课程图片
        Optional<CoursePic> coursePicOptional = coursePicRepository.findById(id);
        if (coursePicOptional.isPresent()) {
            CoursePic coursePic = coursePicOptional.get();
            courseView.setCoursePic(coursePic);
        }
        //查询营销信息
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(id);
        if (courseMarketOptional.isPresent()) {
            CourseMarket courseMarket = courseMarketOptional.get();
            courseView.setCourseMarket(courseMarket);
        }
        //课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;
    }

    public CourseBase findCourseBaseById(String courseId) {
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(courseId);
        return courseBaseOptional.orElse(null);
    }

    //课程预览
    public CoursePublishResult preview(String courseId) {
        //查询信息
        CourseBase courseBase = this.findCourseBaseById(courseId);
        //准备cmsPage信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);
        cmsPage.setTemplateId(publish_templateId);
        //页面请求数据
        cmsPage.setDataUrl(publish_dataUrlPre + courseId);
        //页面名称
        cmsPage.setPageName(courseId + ".html");
        //页面别名
        cmsPage.setPageAliase(courseBase.getName());
        //页面webPath
        cmsPage.setPageWebPath(publish_page_webpath);
        //页面物理路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        //请求cms添加页面
        //远程调用cms
        CmsPageResult cmsPageResult = cmsPageClient.saveCmsPage(cmsPage);
        if (!cmsPageResult.isSuccess()) {
            return new CoursePublishResult(CommonCode.FAIL  ,null);
        }
        //拼装页面预览信息
        CmsPage resultCmsPage = cmsPageResult.getCmsPage();
        String pageId = resultCmsPage.getPageId();
        String url = previewUrl + pageId;
        //返回CoursePublishResult,其中包含了页面预览的url
        return new CoursePublishResult(CommonCode.SUCCESS, url);
    }

    //课程发布
    @Transactional(propagation = Propagation.REQUIRED)
    public CoursePublishResult publish(String courseId) {
        //查询课程
        CourseBase courseBase = this.findCourseBaseById(courseId);
        //准备页面信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);
        cmsPage.setTemplateId(publish_templateId);
        //页面请求数据
        cmsPage.setDataUrl(publish_dataUrlPre + courseId);
        //页面名称
        cmsPage.setPageName(courseId + ".html");
        //页面别名
        cmsPage.setPageAliase(courseBase.getName());
        //页面webPath
        cmsPage.setPageWebPath(publish_page_webpath);
        //页面物理路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        //调用cms一键发布接口发布到服务器
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if (!cmsPostPageResult.isSuccess()) {
            return new CoursePublishResult(CommonCode.FAIL, null);
        }
        //更新课程状态为已发布
        CourseBase saveCoursePubState = this.saveCoursePubState(courseId);
        if (saveCoursePubState == null) {
            return new CoursePublishResult(CommonCode.FAIL, null);
        }

        //保存课程索引信息...
        //先创建一个coursePub对象
        CoursePub coursePub = this.createCoursePub(courseId);
        //将coursePub对象保存到数据库
        this.saveCoursePub(courseId, coursePub);

        //缓存课程的信息...
        //将课程计划媒资信息存储到索引表
        this.saveTeachplanMediaPub(courseId);

        //获得url
        String pageUrl = cmsPostPageResult.getPageUrl();
        return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
    }

    // 将coursePub对象保存到数据库
    private CoursePub saveCoursePub(String courseId,CoursePub coursePub) {
        Optional<CoursePub> optionalCoursePub = coursePubRepository.findById(courseId);
        CoursePub newCoursePub;
        if (optionalCoursePub.isPresent()) {
            newCoursePub = optionalCoursePub.get();
        }else {
            newCoursePub = new CoursePub();
        }
        BeanUtils.copyProperties(coursePub, newCoursePub);
        newCoursePub.setId(courseId);
        //设置时间戳
        //使用到logbash的时候会使用到
        newCoursePub.setTimestamp(new Date());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //课程发布时间
        String pubTime = dateFormat.format(new Date());
        newCoursePub.setPubTime(pubTime);
        //保存课程信息到数据库
        coursePubRepository.save(newCoursePub);
        return newCoursePub;
    }

    //创建一个coursePub对象
    private CoursePub createCoursePub(String courseId) {
        CoursePub coursePub = new CoursePub();
        //根据课程id查询course_base
        //将courseBase属性拷贝到coursePub中
        Optional<CourseBase> optionalCourseBase = courseBaseRepository.findById(courseId);
        if (optionalCourseBase.isPresent()) {
            CourseBase courseBase = optionalCourseBase.get();
            BeanUtils.copyProperties(courseBase, coursePub);
        }
        //根据课程id查询course_pic
        //将courseBase属性拷贝到coursePub中
        Optional<CoursePic> optionalCoursePic = coursePicRepository.findById(courseId);
        if (optionalCoursePic.isPresent()) {
            CoursePic coursePic = optionalCoursePic.get();
            BeanUtils.copyProperties(coursePic, coursePub);
        }
        //根据课程id查询course_market
        //将courseMarket属性拷贝到coursePub中
        Optional<CourseMarket> optionalCourseMarket = courseMarketRepository.findById(courseId);
        if (optionalCourseMarket.isPresent()) {
            CourseMarket courseMarket = optionalCourseMarket.get();
            BeanUtils.copyProperties(courseMarket, coursePub);
        }
        //课程计划信息
        //将课程计划信息json串保存到course_pub中
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        String jsonString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(jsonString);
        return coursePub;
    }

    //更新课程状态为已发布202002
    private CourseBase saveCoursePubState(String courseId) {
        CourseBase courseBase = this.findCourseBaseById(courseId);
        courseBase.setStatus("202002");
        courseBaseRepository.save(courseBase);
        return courseBase;
    }

    //保存课程计划与媒资文件的关联信息
    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        //判断关联信息和课程计划是否为空
        if (teachplanMedia == null || StringUtils.isEmpty(teachplanMedia.getTeachplanId())) {
            ExceptionCast.cast(CommonCode.INVAILD_PARAM);
        }
        //课程计划id
        String teachplanId = teachplanMedia.getTeachplanId();
        //从数据库查询课程计划信息
        Optional<Teachplan> teachplanOptional = teachplanRepository.findById(teachplanId);
        if (!teachplanOptional.isPresent()) {
            ExceptionCast.cast(CommonCode.INVAILD_PARAM);
        }
        Teachplan teachplan = teachplanOptional.get();
        //获取课程等级
        String grade = teachplan.getGrade();
        if (StringUtils.isEmpty(grade) || !grade.equals("3")) {
            //只允许第三级的课程计划和视频关联
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }
        Optional<TeachplanMedia> teachplanMediaOptional = teachplanMediaRepository.findById(teachplanId);
        TeachplanMedia result;
        if (teachplanMediaOptional.isPresent()) {
            result = teachplanMediaOptional.get();
        }else {
            result = new TeachplanMedia();
        }
        result.setTeachplanId(teachplanId);
        result.setCourseId(teachplanMedia.getCourseId());
        result.setMediaUrl(teachplanMedia.getMediaUrl());
        result.setMediaId(teachplanMedia.getMediaId());
        result.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        //将关联信息保存到数据库
        teachplanMediaRepository.save(result);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    //保存课程计划媒资信息
    private void saveTeachplanMediaPub(String courseId) {
        //根据课程id查询课程计划媒资信息
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        //根据课程id删除课程计划媒资信息
        teachplanMediaPubRepository.deleteByCourseId(courseId);

        //将课程计划媒资信息存储到索引表
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        for (TeachplanMedia teachplanMedia : teachplanMediaList) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia, teachplanMediaPub);
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubList.add(teachplanMediaPub);
        }
        teachplanMediaPubRepository.saveAll(teachplanMediaPubList);
    }
}
