package com.ck0731.caregiver.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ck0731.caregiver.entity.Hospital;
import com.ck0731.caregiver.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医院Controller
 * 提供医院搜索和查询功能
 */
@RestController
@RequestMapping("/hospital")
@CrossOrigin(origins = "*")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    /**
     * 搜索医院（支持分页和多条件筛选）
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Hospital>> searchHospitals(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String level
    ) {
        QueryWrapper<Hospital> wrapper = new QueryWrapper<>();

        if (province != null && !province.isEmpty()) {
            wrapper.eq("province", province);
        }

        if (city != null && !city.isEmpty()) {
            wrapper.eq("city", city);
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("name", keyword);
        }

        if (level != null && !level.isEmpty()) {
            wrapper.eq("level", level);
        }

        wrapper.orderByAsc("province", "city", "name");

        Page<Hospital> pageResult = hospitalService.page(new Page<>(page, size), wrapper);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 获取所有省份列表
     */
    @GetMapping("/provinces")
    public ResponseEntity<List<String>> getProvinces() {
        QueryWrapper<Hospital> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT province");
        wrapper.orderByAsc("province");
        List<Hospital> hospitals = hospitalService.list(wrapper);
        List<String> provinces = hospitals.stream()
                .map(Hospital::getProvince)
                .distinct()
                .toList();
        return ResponseEntity.ok(provinces);
    }

    /**
     * 根据省份获取城市列表
     */
    @GetMapping("/cities")
    public ResponseEntity<List<String>> getCities(@RequestParam String province) {
        QueryWrapper<Hospital> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT city");
        wrapper.eq("province", province);
        wrapper.orderByAsc("city");
        List<Hospital> hospitals = hospitalService.list(wrapper);
        List<String> cities = hospitals.stream()
                .map(Hospital::getCity)
                .distinct()
                .toList();
        return ResponseEntity.ok(cities);
    }

    /**
     * 根据ID获取医院详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Hospital> getHospitalById(@PathVariable Long id) {
        Hospital hospital = hospitalService.getById(id);
        if (hospital == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(hospital);
    }

    /**
     * 获取热门医院（按使用频率排序，这里简化为按ID排序）
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Hospital>> getPopularHospitals(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        QueryWrapper<Hospital> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("id");
        wrapper.last("LIMIT " + limit);
        List<Hospital> hospitals = hospitalService.list(wrapper);
        return ResponseEntity.ok(hospitals);
    }
}

