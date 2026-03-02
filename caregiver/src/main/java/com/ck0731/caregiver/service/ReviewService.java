package com.ck0731.caregiver.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ck0731.caregiver.entity.Review;
import com.ck0731.caregiver.mapper.ReviewMapper;
import org.springframework.stereotype.Service;

@Service
public class ReviewService extends ServiceImpl<ReviewMapper, Review> {
}