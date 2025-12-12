package com.shuttleshout.repository;

import org.apache.ibatis.annotations.Mapper;

import com.mybatisflex.core.BaseMapper;
import com.shuttleshout.common.model.po.Court;

/**
 * 場地Repository接口
 * 
 * @author ShuttleShout Team
 */
@Mapper
public interface CourtRepository extends BaseMapper<Court> {
}

