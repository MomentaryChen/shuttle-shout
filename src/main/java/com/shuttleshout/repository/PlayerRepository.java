package com.shuttleshout.repository;

import com.mybatisflex.core.BaseMapper;
import com.shuttleshout.common.model.po.Player;
import org.apache.ibatis.annotations.Mapper;

/**
 * 球員Repository接口
 * 
 * @author ShuttleShout Team
 */
@Mapper
public interface PlayerRepository extends BaseMapper<Player> {
}

