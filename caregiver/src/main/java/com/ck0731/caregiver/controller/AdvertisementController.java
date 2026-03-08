package com.ck0731.caregiver.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ck0731.caregiver.entity.Advertisement;
import com.ck0731.caregiver.service.AdvertisementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/advertisement")
public class AdvertisementController {

    @Autowired
    private AdvertisementService advertisementService;

    // 获取指定位置的广告列表
    @GetMapping("/position/{position}")
    public List<Advertisement> getByPosition(@PathVariable String position) {
        QueryWrapper<Advertisement> wrapper = new QueryWrapper<>();
        wrapper.eq("position", position)
               .eq("status", "active")
               .orderByAsc("sort_order");
        return advertisementService.list(wrapper);
    }

    // 获取所有启用的广告
    @GetMapping("/active")
    public List<Advertisement> getActiveAds() {
        QueryWrapper<Advertisement> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "active")
               .orderByAsc("sort_order");
        return advertisementService.list(wrapper);
    }

    // 创建或更新广告
    @PostMapping("/save")
    public boolean save(@RequestBody Advertisement advertisement) {
        return advertisementService.saveOrUpdate(advertisement);
    }

    // 删除广告
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return advertisementService.removeById(id);
    }
}
