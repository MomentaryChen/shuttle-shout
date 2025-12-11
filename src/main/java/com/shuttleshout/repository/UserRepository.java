package com.shuttleshout.repository;

import com.mybatisflex.core.BaseMapper;
import com.shuttleshout.common.model.po.UserPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Repository接口
 * 
 * @author ShuttleShout Team
 */
@Mapper
public interface UserRepository extends BaseMapper<UserPO> {
}

