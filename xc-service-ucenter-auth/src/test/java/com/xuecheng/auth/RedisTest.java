package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void redisTest(){
        //定义key
        String key = "user_token:5690ccfd-0c48-4eae-8357-212dd0ebdaa0";
        //定义value
        Map<String,String> value = new HashMap<>();
        value.put("jwt", "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6IlhjV2ViQXBwIiwic2NvcGUiOlsiYXBwIl0sIm5hbWUiOm51bGwsInV0eXBlIjpudWxsLCJpZCI6bnVsbCwiZXhwIjoxNTcwOTIzMDc3LCJqdGkiOiI1NjkwY2NmZC0wYzQ4LTRlYWUtODM1Ny0yMTJkZDBlYmRhYTAiLCJjbGllbnRfaWQiOiJYY1dlYkFwcCJ9.BnC9U21J0XvW0UfgCi82cmUWj96DmJcKwmFrgO9LumOWBbDAlObOVFO3salVeaQ3rllGsdrMui87XYI91vCzYOYOxZTuj6G5g2DjpGAvHV_yAmMwMUj387F9FQ-2YYL0MSSmMPX874luo-ewLyJFeTRE8mIIwyaI9rWAGXT9_m5_yOO_XfWhvZA85RKDqz7iv5GVmyHGlm6x-lQt6Uh3t1AdFoqvnAN-fV6tD_VMHzP6K7ez9MkYb7PVXGQxb2IpajpPqYQBCEVqXKsps8EVOENQRYMDZXGebAZ5d6Az-DEgkcLUJIQkDmks9G6pTEMkmpBQBX35sQjlJqZLeKWJUA");
        value.put("refresh_token", "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6IlhjV2ViQXBwIiwic2NvcGUiOlsiYXBwIl0sImF0aSI6IjU2OTBjY2ZkLTBjNDgtNGVhZS04MzU3LTIxMmRkMGViZGFhMCIsIm5hbWUiOm51bGwsInV0eXBlIjpudWxsLCJpZCI6bnVsbCwiZXhwIjoxNTcwOTIzMDc3LCJqdGkiOiIzYWU5YWNmNC1lYTE4LTQyNmQtYWVkMy1mOTg0NzZhMWMxZDgiLCJjbGllbnRfaWQiOiJYY1dlYkFwcCJ9.iv5K9CUtERCVPX-fLfywKhjkRowQ3hocu-1yewJKTJIs_Pv_3XUqsyYpTkzwcWa9jyKzt9z7KHi94XQDrYUf35OgBylpEO6clzagq3ENnbt6TGghEzhtwU9LqUN26v6V2RYTTJAUyduBeMmHsX5cNu_peohKQrEv1hjWkjZxaLnLU_uvraUxaY2w1nAkWmItpfZq5XKmiE2QZ3iq0MzDbEzKUcmohtHNbIYMGJGAqZ0nKZN83dCQBpDLowPBPLiaFMKdo8yBXycHGECleHrfqS0-wf5EiFlpHPMXsU88kd1sfh_cXxjWddOUgrKZr1s7mX-jgGCg709ftBvIcIF2uw");

        String jsonString = JSON.toJSONString(value);

        //存储数据
        stringRedisTemplate.boundValueOps(key).set(jsonString,30, TimeUnit.SECONDS);
        //获取数据
        String string = stringRedisTemplate.opsForValue().get(key);
        System.out.println(string);
        //校验key是否存在
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        System.out.println("过期时间:" + expire);
    }
}
