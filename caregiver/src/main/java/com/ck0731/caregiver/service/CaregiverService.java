package com.ck0731.caregiver.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ck0731.caregiver.entity.Caregiver;
import com.ck0731.caregiver.mapper.CaregiverMapper;
import org.springframework.stereotype.Service;

@Service
public class CaregiverService extends ServiceImpl<CaregiverMapper, Caregiver> {
}