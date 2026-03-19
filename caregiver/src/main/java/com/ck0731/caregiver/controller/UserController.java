package com.ck0731.caregiver.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ck0731.caregiver.entity.User;
import com.ck0731.caregiver.service.SmsService;
import com.ck0731.caregiver.service.UserService;
import com.ck0731.caregiver.util.JwtUtil;
import com.ck0731.caregiver.util.WeChatUtil; // 引入新工具类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
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

    @Autowired
    private WeChatUtil weChatUtil; // 注入微信工具类

    // 根据openid获取用户
    @GetMapping("/openid/{openid}")
    public User getByOpenid(@PathVariable String openid) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("openid", openid);
        return userService.getOne(wrapper);
    }

    // ✅ 修改后的微信登录接口
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        String code = params.get("code"); // 前端传的是 code
        String nickname = params.get("nickname");
        String avatar = params.get("avatar");
        String userType = params.get("userType");

        Map<String, Object> result = new HashMap<>();

        if (code == null || code.isEmpty()) {
            result.put("success", false);
            result.put("message", "缺少登录凭证 code");
            return result;
        }

        try {
            // 1. 后端用 code 去微信服务器换取 openid (核心步骤)
            String openid = weChatUtil.getOpenId(code);

            // 2. 根据 openid 查询用户
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("openid", openid);
            User user = userService.getOne(wrapper);

            if (user == null) {
                // 新用户，创建账号
                user = new User();
                user.setOpenid(openid); // 存入真实的 openid
                user.setNickname(nickname);
                user.setAvatar(avatar);
                user.setUserType(userType != null ? userType : "client");
                userService.save(user);
                result.put("isNew", true);
            } else {
                // 老用户，可选：更新昵称和头像（因为用户可能修改过微信资料）
                if (nickname != null) user.setNickname(nickname);
                if (avatar != null) user.setAvatar(avatar);
                if (userType != null) user.setUserType(userType);
                userService.updateById(user);
                result.put("isNew", false);
            }

            // 3. 生成 JWT token
            String token = jwtUtil.generateToken(user.getId(), user.getUserType());

            result.put("success", true);
            result.put("user", user);
            result.put("token", token);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "微信登录失败：" + e.getMessage());
            return result;
        }
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

    /**
     * 登录后设置/更新昵称（必须登录，通过 JWT 识别当前用户）
     */
    @PatchMapping("/nickname")
    public Map<String, Object> updateNickname(
            @RequestBody Map<String, String> params,
            HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        String nickname = params != null ? params.get("nickname") : null;
        if (nickname == null || (nickname = nickname.trim()).isEmpty()) {
            result.put("success", false);
            result.put("message", "昵称不能为空");
            return result;
        }
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录或登录已过期");
            return result;
        }
        User user = userService.getById(userId);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        user.setNickname(nickname);
        userService.updateById(user);
        result.put("success", true);
        result.put("user", user);
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