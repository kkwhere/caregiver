package com.ck0731.caregiver.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ck0731.caregiver.entity.User;
import com.ck0731.caregiver.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
}