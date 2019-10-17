package com.xuecheng.govern.gateway.service;

import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //从请求头中取出JWT令牌
    public String getJwtFromHeader(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isEmpty(authorization)) {
            return null;
        }
        return authorization;
    }

    //从cookie中取出用户身份令牌
    public String getTokenFromCookie(HttpServletRequest request) {
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        if (map != null && StringUtils.isNotEmpty(map.get("uid"))) {
            return map.get("uid");
        }
        return null;
    }

    //从redis中查询用身份令牌有效期
    public Long getExpires(String access_token) {
        String key = "user_token:" + access_token;
        return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
    }
}
