package com.ck0731.caregiver.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ck0731.caregiver.entity.Review;
import com.ck0731.caregiver.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/caregiver/{caregiverId}")
    public List<Review> getByCaregiverId(@PathVariable Long caregiverId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("caregiver_id", caregiverId);
        return reviewService.list(wrapper);
    }
}