package com.shuttleshout.repository;

import java.time.LocalDateTime;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.mybatisflex.core.BaseMapper;
import com.shuttleshout.common.model.po.Court;

/**
 * 場地Repository接口
 * 
 * @author ShuttleShout Team
 */
@Mapper
public interface CourtRepository extends BaseMapper<Court> {
    
    /**
     * 清空場地的球員信息和比賽時間
     * 使用 SQL 直接更新，確保 null 值被正確寫入數據庫
     * 
     * @param courtId 場地ID
     * @param matchEndedAt 比賽結束時間
     * @param updatedAt 更新時間
     */
    @Update("UPDATE team_courts SET " +
            "player1_id = NULL, " +
            "player2_id = NULL, " +
            "player3_id = NULL, " +
            "player4_id = NULL, " +
            "match_started_at = NULL, " +
            "match_ended_at = #{matchEndedAt}, " +
            "updated_at = #{updatedAt} " +
            "WHERE id = #{courtId}")
    void clearCourtPlayers(@Param("courtId") Long courtId, 
                          @Param("matchEndedAt") LocalDateTime matchEndedAt,
                          @Param("updatedAt") LocalDateTime updatedAt);
}

