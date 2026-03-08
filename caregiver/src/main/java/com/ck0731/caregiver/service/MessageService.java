package com.ck0731.caregiver.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ck0731.caregiver.entity.Message;
import com.ck0731.caregiver.mapper.MessageMapper;
import org.springframework.stereotype.Service;

@Service
public class MessageService extends ServiceImpl<MessageMapper, Message> {
}
