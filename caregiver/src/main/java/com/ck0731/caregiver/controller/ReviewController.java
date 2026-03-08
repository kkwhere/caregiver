package com.ck0731.caregiver.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ck0731.caregiver.entity.Caregiver;
import com.ck0731.caregiver.entity.Review;
import com.ck0731.caregiver.service.CaregiverService;
import com.ck0731.caregiver.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private CaregiverService caregiverService;

    // 获取护理员的评价列表
    @GetMapping("/caregiver/{caregiverId}")
    public List<Review> getByCaregiverId(@PathVariable Long caregiverId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("caregiver_id", caregiverId)
               .orderByDesc("create_time");
        return reviewService.list(wrapper);
    }

    // 创建评价（客户评价护理员）
    @PostMapping("/create")
    public boolean create(@RequestBody Review review) {
        boolean success = reviewService.save(review);

        if (success) {
            // 更新护理员的平均评分和评价数量
            updateCaregiverRating(review.getCaregiverId());
        }

        return success;
    }

    // 更新护理员的平均评分和评价数量
    private void updateCaregiverRating(Long caregiverId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("caregiver_id", caregiverId);
        List<Review> reviews = reviewService.list(wrapper);

        if (!reviews.isEmpty()) {
            double totalRating = 0;
            for (Review r : reviews) {
                totalRating += r.getRating();
            }
            double avgRating = totalRating / reviews.size();

            Caregiver caregiver = caregiverService.getById(caregiverId);
            if (caregiver != null) {
                caregiver.setRatingAvg(Math.round(avgRating * 10.0) / 10.0);
                caregiver.setReviewCount(reviews.size());
                caregiverService.updateById(caregiver);
            }
        }
    }

    // 根据签约ID获取评价
    @GetMapping("/contract/{contractId}")
    public Review getByContractId(@PathVariable Long contractId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("contract_id", contractId);
        return reviewService.getOne(wrapper);
    }

    // 获取用户的评价列表
    @GetMapping("/user/{userId}")
    public List<Review> getByUserId(@PathVariable Long userId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .orderByDesc("create_time");
        return reviewService.list(wrapper);
    }
}