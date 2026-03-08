package com.ck0731.caregiver.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 短信服务
 * 当前为模拟实现，实际使用时需要集成真实的短信服务商（如阿里云短信、腾讯云短信等）
 */
@Service
public class SmsService {

    // 存储验证码（手机号 -> 验证码）
    // 实际生产环境应使用Redis存储，并设置过期时间
    private final Map<String, CodeInfo> codeStore = new ConcurrentHashMap<>();

    // 验证码有效期（毫秒）：5分钟
    private static final long CODE_EXPIRATION = 5 * 60 * 1000;

    /**
     * 发送验证码
     * @param phone 手机号
     * @return 验证码（实际生产环境不应返回，这里为了测试方便）
     */
    public String sendCode(String phone) {
        // 生成6位随机验证码
        String code = generateCode();

        // 存储验证码和发送时间
        codeStore.put(phone, new CodeInfo(code, System.currentTimeMillis()));

        // TODO: 实际生产环境中，这里应该调用短信服务商API发送验证码
        // 例如：阿里云短信、腾讯云短信等
        System.out.println("发送验证码到手机号 " + phone + ": " + code);

        // 返回验证码（仅用于测试，生产环境不应返回）
        return code;
    }

    /**
     * 验证验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 是否验证成功
     */
    public boolean verifyCode(String phone, String code) {
        CodeInfo codeInfo = codeStore.get(phone);

        if (codeInfo == null) {
            return false;
        }

        // 检查验证码是否过期
        if (System.currentTimeMillis() - codeInfo.timestamp > CODE_EXPIRATION) {
            codeStore.remove(phone);
            return false;
        }

        // 验证码匹配
        boolean isValid = codeInfo.code.equals(code);

        // 验证成功后删除验证码（一次性使用）
        if (isValid) {
            codeStore.remove(phone);
        }

        return isValid;
    }

    /**
     * 生成6位随机验证码
     */
    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * 验证码信息类
     */
    private static class CodeInfo {
        String code;
        long timestamp;

        CodeInfo(String code, long timestamp) {
            this.code = code;
            this.timestamp = timestamp;
        }
    }
}
