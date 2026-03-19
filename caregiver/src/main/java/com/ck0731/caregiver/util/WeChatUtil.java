package com.ck0731.caregiver.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Component
public class WeChatUtil {

    // 从配置文件读取，例如 application.yml: wechat.miniapp.appid: xxx
    @Value("${wechat.miniapp.appid}")
    private String appid;

    @Value("${wechat.miniapp.secret}")
    private String secret;

    /**
     * 通过 code 换取 openid
     */
    public String getOpenId(String code) throws Exception {
        String url = "https://api.weixin.qq.com/sns/jscode2session" +
                "?appid=" + appid +
                "&secret=" + secret +
                "&js_code=" + code +
                "&grant_type=authorization_code";

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        conn.disconnect();

        JSONObject json = JSON.parseObject(sb.toString());
        if (json.containsKey("openid")) {
            return json.getString("openid");
        } else {
            // 错误处理，比如 code 无效或过期
            throw new RuntimeException("获取OpenID失败：" + json.getString("errmsg"));
        }
    }
}