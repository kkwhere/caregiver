package com.ck0731.caregiver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("caregiver")
public class Caregiver {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String avatar;
    private String gender; // male-男, female-女
    private LocalDate birthDate;
    private String province;
    private String city;
    private String hospital;
    private String intro;
    private String status; // available-空闲, busy-护理中
    private Double ratingAvg;
    private Integer reviewCount;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}