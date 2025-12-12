package com.shuttleshout.repository;

import org.apache.ibatis.annotations.Mapper;

import com.mybatisflex.core.BaseMapper;
import com.shuttleshout.common.model.po.Match;

/**
 * 比賽Repository接口
 * 
 * @author ShuttleShout Team
 */
@Mapper
public interface MatchRepository extends BaseMapper<Match> {
}

