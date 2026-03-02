package com.ck0731.caregiver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("caregiver")
public class Caregiver {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String avatar;
    private String hospital;
    private String intro;
    private Double ratingAvg;
    private LocalDateTime createTime;
}