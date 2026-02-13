package com.shuttleshout.repository;

import com.mybatisflex.core.BaseMapper;
import com.shuttleshout.common.model.po.Queue;
import org.apache.ibatis.annotations.Mapper;

/**
 * 隊列Repository接口
 * 
 * @author ShuttleShout Team
 */
@Mapper
public interface QueueRepository extends BaseMapper<Queue> {
}

