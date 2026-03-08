package com.ck0731.caregiver.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ck0731.caregiver.entity.Message;
import com.ck0731.caregiver.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    // 获取用户的消息列表
    @GetMapping("/user/{userId}")
    public List<Message> getUserMessages(@PathVariable Long userId) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", userId)
               .orderByDesc("create_time");
        return messageService.list(wrapper);
    }

    // 获取用户的未读消息列表
    @GetMapping("/unread/{userId}")
    public List<Message> getUnreadMessages(@PathVariable Long userId) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", userId)
               .eq("is_read", 0)
               .orderByDesc("create_time");
        return messageService.list(wrapper);
    }

    // 标记消息为已读
    @PutMapping("/read/{id}")
    public boolean markAsRead(@PathVariable Long id) {
        Message message = messageService.getById(id);
        if (message != null) {
            message.setIsRead(1);
            return messageService.updateById(message);
        }
        return false;
    }

    // 批量标记消息为已读
    @PutMapping("/read-batch")
    public boolean markBatchAsRead(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            Message message = messageService.getById(id);
            if (message != null) {
                message.setIsRead(1);
                messageService.updateById(message);
            }
        }
        return true;
    }

    // 删除消息
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return messageService.removeById(id);
    }
}
