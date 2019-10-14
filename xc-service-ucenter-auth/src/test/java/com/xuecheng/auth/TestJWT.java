package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.hibernate.sql.Alias;
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
import java.security.PrivateKey;
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
        String publicKey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnASXh9oSvLRLxk901HANYM6KcYMzX8vFPnH/To2R+SrUVw1O9rEX6m1+rIaMzrEKPm12qPjVq3HMXDbRdUaJEXsB7NgGrAhepYAdJnYMizdltLdGsbfyjITUCOvzZ/QgM1M4INPMD+Ce859xse06jnOkCUzinZmasxrmgNV3Db1GtpyHIiGVUY0lSO1Frr9m5dpemylaT0BV3UwTQWVW9ljm6yR3dBncOdDENumT5tGbaDVyClV0FEB1XdSKd7VjiDCDbUAUbDTG1fm3K9sx7kO1uMGElbXLgMfboJ963HEJcU01km7BmFntqI5liyKheX+HBUCD4zbYNPw236U+7QIDAQAB-----END PUBLIC KEY-----";
        //jwt令牌
        String jwtString = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiR2VsaWdhbWVzaCJ9.RgFnTwinlKKOxLTbRrHDnTmJlfKajv3E_6M3Fh5pLVGdNO80AK0KHqZ69J0TsmsyRd1G8X0CTpATdneXScWI1Pc01wdgc0ubVDBcRlx2wndA0SoeXkQMQcMh2mMhL3aGKd4Z9tEkgOdO-73JK2z5ke3qt-TUtzKOHISqr0agFYU8Q5TXAe-mR0pcQI3SWKaWe2fYRqmCgggzV_KP9zeGwsassBbQnw8rxUjczPP356b5WunEJ26DkgPbreqkJnXvUJzmulpQgA8qn9vBbXcOiYyLJDp6L3QJnSduRsQOiQNcxYz1jsztUGIUBjL3z7IGdL3bQ2wR_GpNtieNZAzyFQ";
        //校验jwt令牌
        Jwt jwt = JwtHelper.decodeAndVerify(jwtString, new RsaVerifier(publicKey));
        String jwtClaims = jwt.getClaims();
        //获取jwt中自定义的内容
        System.out.println(jwtClaims);
    }
}
