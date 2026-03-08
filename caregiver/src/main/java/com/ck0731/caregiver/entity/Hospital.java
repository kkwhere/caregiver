package com.ck0731.caregiver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 医院实体类
 */
@Data
@TableName("hospital")
public class Hospital {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String province;

    private String city;

    private String district;

    private String address;

    private String level;

    private String type;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
