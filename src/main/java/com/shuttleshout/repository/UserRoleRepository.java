package com.shuttleshout.repository;

import org.apache.ibatis.annotations.Mapper;

import com.mybatisflex.core.BaseMapper;
import com.shuttleshout.common.model.po.UserRolePO;

/**
 * 用户角色关联Repository接口
 * 
 * @author ShuttleShout Team
 */
@Mapper
public interface UserRoleRepository extends BaseMapper<UserRolePO> {
}

