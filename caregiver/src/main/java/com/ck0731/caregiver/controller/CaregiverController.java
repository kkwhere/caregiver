package com.ck0731.caregiver.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ck0731.caregiver.entity.Caregiver;
import com.ck0731.caregiver.service.CaregiverService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/caregiver")
public class CaregiverController {

    @Autowired
    private CaregiverService caregiverService;

    // 获取所有护理员列表
    @GetMapping("/list")
    public List<Caregiver> list() {
        return caregiverService.list();
    }

    // 根据ID获取护理员详情
    @GetMapping("/{id}")
    public Caregiver getById(@PathVariable Long id) {
        return caregiverService.getById(id);
    }

    // 获取热门护理员（前3名）
    @GetMapping("/hot")
    public List<Caregiver> getHotCaregivers() {
        QueryWrapper<Caregiver> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("rating_avg", "review_count")
               .last("LIMIT 3");
        return caregiverService.list(wrapper);
    }

    // 分页查询护理员（支持筛选）
    @GetMapping("/search")
    public IPage<Caregiver> search(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String hospital,
            @RequestParam(required = false) String status
    ) {
        Page<Caregiver> pageParam = new Page<>(page, size);
        QueryWrapper<Caregiver> wrapper = new QueryWrapper<>();

        if (gender != null && !gender.isEmpty()) {
            wrapper.eq("gender", gender);
        }
        if (province != null && !province.isEmpty()) {
            wrapper.eq("province", province);
        }
        if (city != null && !city.isEmpty()) {
            wrapper.eq("city", city);
        }
        if (hospital != null && !hospital.isEmpty()) {
            wrapper.like("hospital", hospital);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }

        // 年龄筛选（根据出生日期计算）
        if (minAge != null) {
            LocalDate maxBirthDate = LocalDate.now().minusYears(minAge);
            wrapper.le("birth_date", maxBirthDate);
        }
        if (maxAge != null) {
            LocalDate minBirthDate = LocalDate.now().minusYears(maxAge + 1);
            wrapper.ge("birth_date", minBirthDate);
        }

        wrapper.orderByDesc("rating_avg");
        return caregiverService.page(pageParam, wrapper);
    }

    // 根据用户ID获取护理员信息
    @GetMapping("/user/{userId}")
    public Caregiver getByUserId(@PathVariable Long userId) {
        QueryWrapper<Caregiver> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return caregiverService.getOne(wrapper);
    }

    // 创建或更新护理员资料
    @PostMapping("/save")
    public boolean save(@RequestBody Caregiver caregiver) {
        return caregiverService.saveOrUpdate(caregiver);
    }

    // 更新护理员状态
    @PutMapping("/status/{id}")
    public boolean updateStatus(@PathVariable Long id, @RequestParam String status) {
        Caregiver caregiver = caregiverService.getById(id);
        if (caregiver != null) {
            caregiver.setStatus(status);
            return caregiverService.updateById(caregiver);
        }
        return false;
    }
}
