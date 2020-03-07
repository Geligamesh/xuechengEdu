package com.xuecheng.manage_cms.controller;

import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.PageService;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
@Api(value = "cms配置预览接口",description = "cms配置预览接口,页面模板和数据模型静态化之后的预览展示")
public class CmsPagePreviewController extends BaseController {

    @Autowired
    private PageService pageService;

    @GetMapping("cms/preview/{pageId}")
    public void preview(@PathVariable String pageId) {
        String pageHtml = pageService.getPageHtml(pageId);
        if (StringUtils.isNotEmpty(pageHtml)) {
            try {
                ServletOutputStream outputStream = response.getOutputStream();
                response.setHeader("Content-type","text/html;charset=utf-8");
                outputStream.write(pageHtml.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
