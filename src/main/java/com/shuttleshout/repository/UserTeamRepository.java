package com.shuttleshout.repository;

import org.apache.ibatis.annotations.Mapper;

import com.mybatisflex.core.BaseMapper;
import com.shuttleshout.common.model.po.UserTeamPO;

/**
 * 球队用户关联Repository接口
 * 
 * @author ShuttleShout Team
 */
@Mapper
public interface UserTeamRepository extends BaseMapper<UserTeamPO> {
}

