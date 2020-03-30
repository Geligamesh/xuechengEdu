package com.xuecheng.govern.gateway.fliter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginFilter extends ZuulFilter {

    @Autowired
    private AuthService authService;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext requestContext = RequestContext.getCurrentContext();
        //得到request
        HttpServletRequest request = requestContext.getRequest();
        //得到response
        HttpServletResponse response = requestContext.getResponse();
        //从cookie中取出用户身份令牌
        String uid = authService.getTokenFromCookie(request);
        if (StringUtils.isEmpty(uid)) {
            //拒绝访问
            access_denied();
            return null;
        }
        //从请求头中取出JWT令牌
        String jwt = authService.getJwtFromHeader(request);
        if (StringUtils.isEmpty(jwt)) {
            //拒绝访问
            // access_denied();
            return null;
        }
        //从redis中查询用身份令牌有效期
        Long expires = authService.getExpires(uid);
        if (expires < 0) {
            //拒绝访问
            access_denied();
            return null;
        }
        return null;
    }

    //拒绝访问
    private void access_denied() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        //得到response
        HttpServletResponse response = requestContext.getResponse();
        //拒绝访问
        requestContext.setSendZuulResponse(false);
        //设置响应代码
        requestContext.setResponseStatusCode(200);
        //构建响应的信息
        ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
        //转成json
        String jsonString = JSON.toJSONString(responseResult);
        requestContext.setResponseBody(jsonString);
        //转成json，设置contentType
        response.setContentType("application/json;charset=utf-8");
    }
}
