package com.ck0731.caregiver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("contract")
public class Contract {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long caregiverId;
    private Long clientId;
    private String status; // pending, accepted, rejected, in_service, end_requested, completed, cancelled
    private LocalDateTime requestTime;
    private LocalDateTime acceptTime;
    private LocalDateTime startTime;
    private LocalDateTime endRequestTime;
    private LocalDateTime endTime;
    private String remark;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
