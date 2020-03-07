package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PageService {

    @Autowired
    private CmsPageRepository cmsPageRepository;
    @Autowired
    private CmsConfigRepository cmsConfigRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFSBucket gridFSBucket;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private CmsSiteRepository cmsSiteRepository;

    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {

        if (queryPageRequest == null) {
            queryPageRequest = new QueryPageRequest();
        }

        //自定义条件查询
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("pageType", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("pageName", ExampleMatcher.GenericPropertyMatchers.contains());
        CmsPage cmsPage = new CmsPage();
        //设置站点id
        if(StringUtils.isNotEmpty(queryPageRequest.getSiteId())){
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        //设置模板id
        if (StringUtils.isNotEmpty(queryPageRequest.getTemplateId())) {
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        //设置页面别名
        if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())) {
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        //设置页面名称
        if (StringUtils.isNotEmpty(queryPageRequest.getPageName())) {
            cmsPage.setPageName(queryPageRequest.getPageName());
        }
        //设置页面类型
        if (StringUtils.isNotEmpty(queryPageRequest.getPageType())) {
            cmsPage.setPageType(queryPageRequest.getPageType());
        }

        //设置自定义查询
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);
        if(page <= 0) {
            page = 1;
        }
        page = page - 1;
        if(size <= 0) {
            size = 10;
        }
        Pageable pageable = PageRequest.of(page, size);
        //实现自定义和分页查询结果
        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);
        QueryResult queryResult = new QueryResult();
        queryResult.setList(all.getContent());
        queryResult.setTotal(all.getTotalElements());
        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
    }

    //新增页面
    public CmsPageResult add(CmsPage cmsPage) {

        if (cmsPage == null) {
            //模板为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //检验页面名称，站点id，页面webpath的唯一性
        //根据页面名称，站点id,页面webpath去cms_path集合，如果查到说明页面已经存在了，如果查询不到在继续添加
        CmsPage newCmsPage = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());

        if (newCmsPage!=null) {
            //页面已经存在
            //抛出与异常，异常内容就是页面已经存在
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        //新增页面
        cmsPage.setPageId(null);
        cmsPageRepository.save(cmsPage);
        return new CmsPageResult(CommonCode.SUCCESS,cmsPage);
    }

    //根据页面id查询页面
    public CmsPage getById(String id) {
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        return optional.orElse(null);
    }

    //修改页面
    public CmsPageResult update(String id,CmsPage cmsPage) {
        CmsPage result = this.getById(id);
        if (result!=null) {
            result.setTemplateId(cmsPage.getTemplateId());
            result.setSiteId(cmsPage.getSiteId());
            result.setPageAliase(cmsPage.getPageAliase());
            result.setPageName(cmsPage.getPageName());
            result.setPageWebPath(cmsPage.getPageWebPath());
            result.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            result.setDataUrl(cmsPage.getDataUrl());
            cmsPageRepository.save(result);
            return new CmsPageResult(CommonCode.SUCCESS, result);
        }
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    //根据id删除页面
    public ResponseResult delete(String id) {
        CmsPage cmsPage = this.getById(id);
        if (cmsPage!=null) {
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    //根据id查询CmsConfig
    public CmsConfig getCmsConfigById(String id){
        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        if (optional.isPresent()) {
            CmsConfig cmsConfig = optional.get();
            return cmsConfig;
        }
        return null;
    }

    //页面静态化方法
    //静态化程序获取页面的DataUrl
    public String getPageHtml(String pageId) {
        //获取数据模型
        Map model = getModelByPageId(pageId);
        //如果数据模型不存在
        if (model == null) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        String templateContent = this.getTemplateByPageId(pageId);
        if (StringUtils.isEmpty(templateContent)) {
            //当页面模板为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //执行到此说明数据模型存在,页面模板也不为空
        //则执行静态化
        String html = this.generateHtml(templateContent, model);
        if (StringUtils.isEmpty(html)) {
            //生成的静态化为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        return html;
    }

    //页面静态化
    public String generateHtml(String templateContent,Map model) {
        try {
            //生成配置类
            Configuration configuration = new Configuration(Configuration.getVersion());
            //模板加载器
            StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
            stringTemplateLoader.putTemplate("template", templateContent);
            //设置模板加载器
            configuration.setTemplateLoader(stringTemplateLoader);

            //获取模板
            Template template = configuration.getTemplate("template");
            //页面静态化
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return html;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取页面模板
    private String getTemplateByPageId(String pageId) {
        //查询页面信息
        CmsPage cmsPage = this.getById(pageId);
        //页面不存在
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //获取模板id
        String templateId = cmsPage.getTemplateId();
        //当页面模板为空时
        if (StringUtils.isEmpty(templateId)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //从cmsTemplate这个集合中查找页面模板
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if (optional.isPresent()) {
            CmsTemplate cmsTemplate = optional.get();
            //获取模板文件id
            String templateFileId = cmsTemplate.getTemplateFileId();
            //根据id查询文件
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开一个下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建GridFsResource,用于获取流对象
            GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
            //从流中取数据
            String content;
            try {
                content = IOUtils.toString(gridFsResource.getInputStream(), StandardCharsets.UTF_8);
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Map getModelByPageId(String pageId) {
        CmsPage cmspage = this.getById(pageId);
        if (cmspage == null) {
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        String dataUrl = cmspage.getDataUrl();
        if (StringUtils.isEmpty(dataUrl)) {
            //dataUrl不存在
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        // 获得全局的request对象
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authorization = request.getHeader("Authorization");
        System.out.println(authorization);

        //请求访问model数据
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        return body;
    }

    //页面发布
    public ResponseResult post(String pageId) {
        //执行页面静态化
        String pageHtml = this.getPageHtml(pageId);
        //将页面静态化文件存储到GridFS中
        CmsPage cmsPage = this.saveHtml(pageId, pageHtml);
        // 向MQ发消息
        this.sendPostPage(pageId);
        return ResponseResult.SUCCESS();
    }

    //向MQ发送消息
    private void sendPostPage(String pageId) {
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage == null) {
            ExceptionCast.cast(CommonCode.INVAILD_PARAM);
        }
        String siteId = cmsPage.getSiteId();
        //创建消息对象
        Map<String,String> msg = new HashMap<>();
        msg.put("pageId", pageId);
        String jsonString = JSON.toJSONString(msg);
        //发送给mq
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE, siteId, jsonString);
    }

    //保存html到GridFS中
    private CmsPage saveHtml(String pageId,String content) {
        //先得到页面信息
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage == null) {
            ExceptionCast.cast(CommonCode.INVAILD_PARAM);
        }
        ObjectId objectId = null;
        //将html文件内容转换成输入流
        try {
            InputStream inputStream = IOUtils.toInputStream(content, "utf-8");
            //将html文件内容存储在GridFS中
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //将html文件id更新到cmsPage中
        cmsPage.setHtmlFileId(objectId.toString());
        cmsPageRepository.save(cmsPage);
        return cmsPage;
    }

    //保存页面，有则更新，没有则添加
    public CmsPageResult save(CmsPage cmsPage) {
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //判断要保存的页面是否存在
        CmsPage newCmsPage = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (newCmsPage != null) {
            //需要保存的页面存在则更新页面
            return this.update(newCmsPage.getPageId(), cmsPage);
        }
        //如果不存在则添加压面
        return this.add(cmsPage);
    }

    //一键发布页面
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
        //将页面信息存储到cms_page中
        CmsPageResult cmsPageResult = this.save(cmsPage);
        if (!cmsPageResult.isSuccess()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //得到页面id
        CmsPage resultCmsPage = cmsPageResult.getCmsPage();
        String pageId = resultCmsPage.getPageId();
        //执行页面静态化（先静态化，再保存到GridFS中，再想MQ发送消息）
        ResponseResult post = this.post(pageId);
        if (!post.isSuccess()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //拼装Url = cmsSite.siteDomain+cmsSite.siteWebPath+ cmsPage.pageWebPath + cmsPage.pageName
        //得到站点id
        String siteId = resultCmsPage.getSiteId();
        //根据站点id获取站点信息
        CmsSite cmsSite = this.findCmsSiteById(siteId);
        //返回pageUrl
        String pageUrl = cmsSite.getSiteDomain() + cmsSite.getSiteWebPath() + resultCmsPage.getPageWebPath() + resultCmsPage.getPageName();
        return new CmsPostPageResult(CommonCode.SUCCESS, pageUrl);
    }

    private CmsSite findCmsSiteById(String siteId) {
        Optional<CmsSite> siteOptional = cmsSiteRepository.findById(siteId);
        return siteOptional.orElse(null);
    }
}
