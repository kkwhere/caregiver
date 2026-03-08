package com.ck0731.caregiver.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ck0731.caregiver.entity.Hospital;
import com.ck0731.caregiver.mapper.HospitalMapper;
import org.springframework.stereotype.Service;

/**
 * 医院Service
 */
@Service
public class HospitalService extends ServiceImpl<HospitalMapper, Hospital> {
}
