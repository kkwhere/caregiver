package com.ck0731.caregiver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long contractId;
    private Long caregiverId;
    private Long userId;
    private Integer rating; // 1-10分
    private String content;
    private String images; // JSON格式存储图片URL数组
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
}