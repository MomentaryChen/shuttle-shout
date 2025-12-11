package com.shuttleshout.service;

import java.util.List;

import javax.validation.Valid;

import com.shuttleshout.common.model.dto.UserTeamCreateDTO;
import com.shuttleshout.common.model.dto.UserTeamDTO;

/**
 * 用户团队关系服务接口
 *
 * @author ShuttleShout Team
 */
public interface UserTeamService {

    /**
     * 获取所有用户团队关系
     */
    List<UserTeamDTO> getAllUserTeams();

    /**
     * 根据团队ID获取团队成员
     */
    List<UserTeamDTO> getTeamMembers(Long teamId);

    /**
     * 根据用户ID获取用户加入的团队
     */
    List<UserTeamDTO> getUserTeams(Long userId);

    /**
     * 用户加入团队
     */
    UserTeamDTO joinTeam(@Valid UserTeamCreateDTO userTeamCreateDto);

    /**
     * 用户离开团队
     */
    void leaveTeam(Long userId, Long teamId);

    /**
     * 团队所有者移除成员
     * @param removerUserId 执行移除操作的用户ID（必须是团队所有者）
     * @param targetUserId 被移除的用户ID
     * @param teamId 团队ID
     */
    void removeMember(Long removerUserId, Long targetUserId, Long teamId);
}
