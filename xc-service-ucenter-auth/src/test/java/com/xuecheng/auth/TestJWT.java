package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.test.context.junit4.SpringRunner;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestJWT {

    //创建JWT令牌
    @Test
    public void testCreateJWT() {
        //密钥库文件
        String keystore = "xc.keystore";
        //密钥库的密码
        String keystore_password = "xuechengkeystore";
        //密钥库文件路径
        ClassPathResource classPathResource = new ClassPathResource(keystore);

        //密钥别名
        String alias = "xckey";
        //密码的访问密码
        String key_password = "xuecheng";

        //创建密钥工厂
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(classPathResource,keystore_password.toCharArray());
        //密钥对(公钥和私钥)
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, key_password.toCharArray());
        //获取私钥
        RSAPrivateKey aPrivate = (RSAPrivateKey) keyPair.getPrivate();
        //JWT令牌的内容
        Map<String,String> body = new HashMap<>();
        body.put("name", "Geligamesh");
        String bodyString = JSON.toJSONString(body);
        //生成JWT
        Jwt jwt = JwtHelper.encode(bodyString, new RsaSigner(aPrivate));

        //生成jwt令牌编码
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }

    //校验jwt令牌
    @Test
    public void testVerify() {
        String publicKey = "";
        //jwt令牌
        String jwtString = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOiIxIiwidXNlcnBpYyI6bnVsbCwidXNlcl9uYW1lIjoiaXRjYXN0Iiwic2NvcGUiOlsiYXBwIl0sIm5hbWUiOiJ0ZXN0MDIiLCJ1dHlwZSI6IjEwMTAwMiIsImV4cCI6MTU3MTQyMjU1MSwidXNlcmlkIjoiNDkiLCJhdXRob3JpdGllcyI6WyJ4Y190ZWFjaG1hbmFnZXJfY291cnNlX2Jhc2UiLCJ4Y190ZWFjaG1hbmFnZXJfY291cnNlX2RlbCIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfbGlzdCIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfcGxhbiIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2UiLCJjb3Vyc2VfZmluZF9saXN0IiwieGNfdGVhY2htYW5hZ2VyIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9tYXJrZXQiLCJ4Y190ZWFjaG1hbmFnZXJfY291cnNlX3B1Ymxpc2giLCJjb3Vyc2VfZmluZF9waWMiLCJ4Y190ZWFjaG1hbmFnZXJfY291cnNlX2FkZCJdLCJqdGkiOiIwM2JkMzUzYi02OGVmLTQyYTEtYWIxNi1lMzI4NTllZGY0ZmMiLCJjbGllbnRfaWQiOiJYY1dlYkFwcCJ9.Cl_qqTT9fEQHcS8Xcu6U7ePMeKRuBgHufDIkiFSWHyunhSel4S71XAzrlAr8Q21mhq76QDQks_1M5kN05HPdiLFrfCf696WHHzLtW6bCBY-YahidWOc2noReXoLOm_aXJVTQ_ckknj_5qN3ovVwIDxegyBIbrCjLnSv5xWDplvKDQuuhq4qhhFuMhwyso6qFB7zpD34T4c8sLux2LFJUzWsn5ysbdt7-dsOEiTUvoBnUB7ijaKtyb02xpob98shKHJVpJWQ37zsQScsCFRPkVL9bTqOpZNHl9ykoquRnJPLCI3ay0iKl9yh6cdKubrNoMApAQe2kLsMHhUBZhbvNog";
        //校验jwt令牌
        Jwt jwt = JwtHelper.decodeAndVerify(jwtString, new RsaVerifier(publicKey));
        String jwtClaims = jwt.getClaims();
        //获取jwt中自定义的内容
        System.out.println(jwtClaims);
    }
}
