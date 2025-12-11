package com.shuttleshout.repository;

import org.apache.ibatis.annotations.Mapper;

import com.mybatisflex.core.BaseMapper;
import com.shuttleshout.common.model.po.RolePO;

/**
 * 角色Repository接口
 * 
 * @author ShuttleShout Team
 */
@Mapper
public interface RoleRepository extends BaseMapper<RolePO> {
}

