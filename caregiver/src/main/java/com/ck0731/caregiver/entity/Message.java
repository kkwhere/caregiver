package com.ck0731.caregiver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String messageType; // contract_request, contract_accept, contract_reject, service_end_request, service_end_accept, service_end_reject
    private Long contractId;
    private String content;
    private Integer isRead; // 0-未读, 1-已读
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
}
