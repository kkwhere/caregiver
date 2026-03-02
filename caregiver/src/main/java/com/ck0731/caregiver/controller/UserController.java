package com.ck0731.caregiver.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ck0731.caregiver.entity.User;
import com.ck0731.caregiver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/openid/{openid}")
    public User getByOpenid(@PathVariable String openid) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("openid", openid);
        return userService.getOne(wrapper);
    }
}