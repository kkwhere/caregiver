package com.ck0731.caregiver.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ck0731.caregiver.entity.User;
import com.ck0731.caregiver.service.SmsService;
import com.ck0731.caregiver.service.UserService;
import com.ck0731.caregiver.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SmsService smsService;

    // 根据openid获取用户
    @GetMapping("/openid/{openid}")
    public User getByOpenid(@PathVariable String openid) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("openid", openid);
        return userService.getOne(wrapper);
    }

    // 微信登录（如果用户不存在则创建，返回JWT token）
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        String openid = params.get("openid");
        String nickname = params.get("nickname");
        String avatar = params.get("avatar");
        String userType = params.get("userType"); // client 或 caregiver

        Map<String, Object> result = new HashMap<>();

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("openid", openid);
        User user = userService.getOne(wrapper);

        if (user == null) {
            // 新用户，创建账号
            user = new User();
            user.setOpenid(openid);
            user.setNickname(nickname);
            user.setAvatar(avatar);
            user.setUserType(userType != null ? userType : "client");
            userService.save(user);
            result.put("isNew", true);
        } else {
            result.put("isNew", false);
        }

        // 生成JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getUserType());

        result.put("user", user);
        result.put("token", token);
        return result;
    }

    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public Map<String, Object> sendCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");

        if (phone == null || phone.isEmpty()) {
            return Map.of("success", false, "message", "手机号不能为空");
        }

        // 验证手机号格式
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return Map.of("success", false, "message", "手机号格式不正确");
        }

        try {
            String code = smsService.sendCode(phone);
            // 注意：生产环境不应返回验证码，这里仅用于测试
            return Map.of(
                "success", true,
                "message", "验证码已发送",
                "code", code  // 仅测试环境返回
            );
        } catch (Exception e) {
            return Map.of("success", false, "message", "发送失败: " + e.getMessage());
        }
    }

    /**
     * 手机验证码登录
     */
    @PostMapping("/phone-login")
    public Map<String, Object> phoneLogin(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String code = params.get("code");
        String userType = params.get("userType"); // client 或 caregiver
        String nickname = params.get("nickname");

        Map<String, Object> result = new HashMap<>();

        // 验证验证码
        if (!smsService.verifyCode(phone, code)) {
            result.put("success", false);
            result.put("message", "验证码错误或已过期");
            return result;
        }

        // 查找或创建用户
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        User user = userService.getOne(wrapper);

        if (user == null) {
            // 新用户，创建账号
            user = new User();
            user.setPhone(phone);
            user.setNickname(nickname != null ? nickname : "用户" + phone.substring(7));
            user.setUserType(userType != null ? userType : "client");
            userService.save(user);
            result.put("isNew", true);
        } else {
            result.put("isNew", false);
        }

        // 生成JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getUserType());

        result.put("success", true);
        result.put("user", user);
        result.put("token", token);
        return result;
    }

    // 更新用户信息
    @PutMapping("/update")
    public boolean update(@RequestBody User user) {
        return userService.updateById(user);
    }

    // 根据ID获取用户
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.getById(id);
    }
}