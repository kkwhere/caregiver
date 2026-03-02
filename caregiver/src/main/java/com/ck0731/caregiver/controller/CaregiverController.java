package com.ck0731.caregiver.controller;

import com.ck0731.caregiver.entity.Caregiver;
import com.ck0731.caregiver.service.CaregiverService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@RestController
@RequestMapping("/caregiver")
public class CaregiverController {

    @Autowired
    private CaregiverService caregiverService;

    @GetMapping("/list")
    public List<Caregiver> list() {
        return caregiverService.list();
    }

    @GetMapping("/{id}")
    public Caregiver getById(@PathVariable Long id) {
        return caregiverService.getById(id);
    }
}