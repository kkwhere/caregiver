package com.ck0731.caregiver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ck0731.caregiver.entity.Hospital;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医院Mapper接口
 */
@Mapper
public interface HospitalMapper extends BaseMapper<Hospital> {
}
