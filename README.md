



学成在线服务端基于Spring Boot构建，采用Spring Cloud微服务框架

| 模块       | **技术栈**                                                   |
| :--------- | ------------------------------------------------------------ |
| 数据访问层 | Spring Data JPA 、Mybatis、Spring Data Mongodb等             |
| 业务层     | Spring IOC、Aop事务控制、Spring Task任务调度、Feign、Ribbon、Spring AMQP、Spring Data Redis |
| 控制层     | Spring MVC、FastJSON、RestTemplate、Spring Security Oauth2+JWT等 |
| 微服务治理 | Eureka、Zuul、Hystrix、Spring Cloud Config等                 |
| 持久层     | MySQL、MongoDB、Redis、ElasticSearch                     |



## 前端项目

> xc-ui-pc-static-portal      门户工程
>
> xc-ui-pc-sysmanage      系统管理前端工程
>
> xc-ui-pc-teach               课程管理前端工程
>
> 



## 后端项目

>xc-framework-parent                      父工程，提供依赖管理
>
>xc-framework-common                  通用工程，提供各层封装
>
>xc-framework-model                      模型工程，提供统一的模型类管理
>
>xc-framework-utils                         工具类工程，提供本项目所使用的工具类
>
>xc-framework-api                           接口工程，统一管理本项目的服务接口
>
>xc-service-manage-cms                CMS页面管理系统(端口31001)
>
>xc‐service‐manage‐cms‐client     CMS页面客户端系统(端口31000)
>
>xc-service-manage-course            课程管理系统(端口31200)
>
>xc-service-base-filesystem            文件上传微服务(22100)
>
>xc-govern-center                           注册中心(50101)

![工程流程图](assets/20200307154408.png)

Nginx的配置信息

```nginx
#cms页面预览
upstream cms_server_pool{
	server 127.0.0.1:31001 weight=10;
}

#静态资源服务
upstream static_server_pool{
    server 127.0.0.1:91 weight=10;
}

#学成网静态资源
server {
    listen 91;
    server_name localhost;

    #公司信息
    location /static/company/ {
        alias E:/mySoftware/xcEdu/xuecheng/static/company/;
    }

    #老师信息
    location /static/teacher/ {
        alias E:/mySoftware/xcEdu/xuecheng/static/teacher/;
    }

    #统计信息
    location /static/stat/ {
        alias E:/mySoftware/xcEdu/xuecheng/static/stat/;
    }

    #课程信息
    location /course/detail/ {
        alias E:/mySoftware/xcEdu/xuecheng/static/course/detail/;
    }

    #分类信息
    location /static/category/ {
        alias E:/mySoftware/xcEdu/xuecheng/static/category/;
    }
}

# 门户信息配置
server{
    listen 80;
    server_name www.xuecheng.com;
    # 在nginx虚拟主机中开通SSI
    ssi on;
    ssi_silent_errors on;

    location / {
        # 路径根据自己配置的修改
        # E:/mySoftware/xcEdu/xc-ui-pc-static-portal/的主目录
        alias E:/mySoftware/xcEdu/xc-ui-pc-static-portal/; 
        index index.html;
    }
    
    #页面预览
    location /cms/preview/ {
	    proxy_pass http://cms_server_pool/cms/preview/;
    }
}

#学成网静态资源
server {
    listen 91;
    server_name localhost;

    #公司信息
    location /static/company/ {
        alias E:/mySoftware/xcEdu/xuecheng/static/company/;
    }

    #老师信息
    location /static/teacher/ {
        alias E:/mySoftware/xcEdu/xuecheng/static/teacher/;
    }

    #统计信息
    location /static/stat/ {
        alias E:/mySoftware/xcEdu/xuecheng/static/stat/;
    }

    #课程信息
    location /course/detail/ {
        alias E:/mySoftware/xcEdu/xuecheng/static/course/detail/;
    }

    #分类信息
    location /static/category/ {
        alias E:/mySoftware/xcEdu/xuecheng/static/category/;
    }
    
    location /static/company/ {
        proxy_pass http://static_server_pool;
    }
    location /static/teacher/ {
        proxy_pass http://static_server_pool;
    }
    location /static/stat/ {
        proxy_pass http://static_server_pool;
    }
    location /course/detail/ {
        proxy_pass http://static_server_pool;
    }

    location /static/img/ {
        alias E:/mySoftware/xcEdu/xc-ui-pc-static-portal/img/;
    }
    location /static/css/ {
        alias E:/mySoftware/xcEdu/xc-ui-pc-static-portal/css/;
    }
    location /static/js/ {
        alias E:/mySoftware/xcEdu/xc-ui-pc-static-portal/js/;
    }
    location /static/plugins/ {
        alias E:/mySoftware/xcEdu/xc-ui-pc-static-portal/plugins/;
        add_header Access‐Control‐Allow‐Origin http://ucenter.xuecheng.com;
        add_header Access‐Control‐Allow‐Credentials true;
        add_header Access‐Control‐Allow‐Methods GET;
    }
    location /plugins/ {
        alias E:/mySoftware/xcEdu/xc-ui-pc-static-portal/plugins/;
        add_header Access‐Control‐Allow‐Origin http://ucenter.xuecheng.com;
        add_header Access‐Control‐Allow‐Credentials true;
        add_header Access‐Control‐Allow‐Methods GET;
    }
}
```

hosts文件的配置（windows系统下）

```
127.0.0.1 www.xuecheng.com
192.168.101.128 img.xuecheng.com
127.0.0.1 video.xuecheng.com
127.0.0.1 teacher.xuecheng.com
127.0.0.1 system.xuecheng.com
```

### 首页

![](https://my-images-bed.oss-cn-hangzhou.aliyuncs.com/images/20200307194700.png)

### 一.CMS页面管理系统核心功能：

1. ####  CMS页面信息的增删改查

2. ####  页面静态化预览

   ##### 2.1 为什么要进行页面管理？  

     本项目cms系统的功能就是根据运营需要，对门户等子系统的部分页面进行管理，从而实现快速根据用户需求修改
   页面内容并上线的需求。  

   ##### 2.2   如何修改页面的内容？  

     在开发中修改页面内容是需要人工编写html及JS文件，CMS系统是通过程序自动化的对页面内容进行修改，通过
   页面静态化技术生成html页面。  

   ##### 2.3   如何对页面进行静态化？  

     一个页面等于模板加数据，在添加页面的时候我们选择了页面的模板。
   页面静态化就是将页面模板和数据通过技术手段将二者合二为一，生成一个html网页文件  ，并将文件存放到由nginx部署的门户页面中。

   ##### 2.4 页面静态化及页面发布流程图如下：  

   <img src="https://my-images-bed.oss-cn-hangzhou.aliyuncs.com/images/20200307194026.png"/>

3. #### 页面发布到门户页面

   ##### 3.1 用户进入教学管理中心，进入某个课程的管理界面

   ##### 3.2 点击课程发布，前端请求到课程管理服务

   ##### 3.3 课程管理服务远程调用CMS生成课程发布页面，CMS将课程详情页面发布到服务器

   ##### 3.4 课程管理服务修改课程发布状态为 “已发布”，并向前端返回发布成功

   ##### 3.5 用户在教学管理中心点击“课程详情页面”链接，查看课程详情页面内容  

   ![](https://my-images-bed.oss-cn-hangzhou.aliyuncs.com/images/20200307223454.png)

![](https://my-images-bed.oss-cn-hangzhou.aliyuncs.com/images/20200307192713.png)

  页面发布流程图如下：  

```
1.前端请求cms执行页面发布。
2.cms执行静态化程序生成html文件。
3.cms将html文件存储到GridFS中。
4.cms向MQ发送页面发布消息
5.MQ将页面发布消息通知给Cms Client
6.Cms Client从GridFS中下载html文件
7.Cms Client将html保存到所在服务器指定目录  
```

![](https://my-images-bed.oss-cn-hangzhou.aliyuncs.com/images/20200307224637.png)

### 二.CMS课程管理系统核心功能：

1. 分类管理

2. 新增课程

3. 修改课程

4. 预览课程

5. 发布课程

6. 上传课程图片

7. 课程预览

   ```
   1、用户进入课程管理页面，点击课程预览，请求到课程管理服务
   2、课程管理服务远程调用cms添加页面接口向cms添加课程详情页面
   3、课程管理服务得到cms返回课程详情页面id，并拼接生成课程预览Url
   4、课程管理服务将课程预览Url给前端返回
   5、用户在前端页面请求课程预览Url，打开新窗口显示课程详情内容
   ```

   

![](https://my-images-bed.oss-cn-hangzhou.aliyuncs.com/images/20200308202527.png)

1. 课程发布

![](https://my-images-bed.oss-cn-hangzhou.aliyuncs.com/images/20200308152116.png)

![](https://my-images-bed.oss-cn-hangzhou.aliyuncs.com/images/20200308194652.png)