package com.ck0731.caregiver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ck0731.caregiver.mapper") // 指定 Mapper 接口包
public class CaregiverApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaregiverApplication.class, args);
    }
}