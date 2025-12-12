package com.shuttleshout.service.impl;

import static com.shuttleshout.common.model.po.table.UserTeamPOTableDef.USER_TEAM_PO;
import static com.shuttleshout.common.model.po.table.UserPOTableDef.USER_PO;
import static com.shuttleshout.common.model.po.table.TeamPOTableDef.TEAM_PO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybatisflex.core.query.QueryWrapper;


import com.shuttleshout.common.exception.ApiException;
import com.shuttleshout.common.exception.ErrorCode;
import com.shuttleshout.common.model.dto.UserTeamCreateDTO;
import com.shuttleshout.common.model.dto.UserTeamDTO;
import com.shuttleshout.common.model.po.TeamPO;
import com.shuttleshout.common.model.po.UserPO;
import com.shuttleshout.common.model.po.UserTeamPO;
import com.shuttleshout.repository.TeamRepository;
import com.shuttleshout.repository.UserRepository;
import com.shuttleshout.repository.UserTeamRepository;
import com.shuttleshout.service.UserTeamService;

/**
 * 用户团队关系服务实现
 *
 * @author ShuttleShout Team
 */
@Service
public class UserTeamServiceImpl implements UserTeamService {

    @Autowired
    private UserTeamRepository userTeamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Override
    public List<UserTeamDTO> getAllUserTeams() {
        List<UserTeamPO> userTeams = userTeamRepository.selectAll();
        return userTeams.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserTeamDTO> getTeamMembers(Long teamId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(USER_TEAM_PO.TEAM_ID.eq(teamId));

        List<UserTeamPO> userTeams = userTeamRepository.selectListByQuery(queryWrapper);
        return userTeams.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserTeamDTO> getUserTeams(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(USER_TEAM_PO.USER_ID.eq(userId));

        List<UserTeamPO> userTeams = userTeamRepository.selectListByQuery(queryWrapper);
        return userTeams.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserTeamDTO joinTeam(@Valid UserTeamCreateDTO userTeamCreateDto) {
        // 检查用户是否存在
        UserPO user = userRepository.selectOneById(userTeamCreateDto.getUserId());
        if (user == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, "用戶不存在，ID: " + userTeamCreateDto.getUserId());
        }

        // 检查团队是否存在
        TeamPO team = teamRepository.selectOneById(userTeamCreateDto.getTeamId());
        if (team == null) {
            throw new ApiException(ErrorCode.TEAM_NOT_FOUND, "團隊不存在，ID: " + userTeamCreateDto.getTeamId());
        }

        // 检查用户是否已经在团队中
        QueryWrapper existingQuery = QueryWrapper.create()
                .where(USER_TEAM_PO.USER_ID.eq(userTeamCreateDto.getUserId()))
                .and(USER_TEAM_PO.TEAM_ID.eq(userTeamCreateDto.getTeamId()));

        UserTeamPO existing = userTeamRepository.selectOneByQuery(existingQuery);
        if (existing != null) {
            throw new ApiException(ErrorCode.USER_ALREADY_IN_TEAM);
        }

        // 创建用户团队关系
        LocalDateTime now = LocalDateTime.now();
        UserTeamPO userTeam = UserTeamPO.builder()
                .userId(userTeamCreateDto.getUserId())
                .teamId(userTeamCreateDto.getTeamId())
                .isOwner(userTeamCreateDto.getIsOwner() != null ? userTeamCreateDto.getIsOwner() : false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        userTeamRepository.insert(userTeam);

        return convertToDto(userTeam);
    }

    @Override
    @Transactional
    public void leaveTeam(Long userId, Long teamId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(USER_TEAM_PO.USER_ID.eq(userId))
                .and(USER_TEAM_PO.TEAM_ID.eq(teamId));

        UserTeamPO userTeam = userTeamRepository.selectOneByQuery(queryWrapper);
        if (userTeam == null) {
            throw new ApiException(ErrorCode.USER_NOT_IN_TEAM);
        }

        // 如果是团队所有者，不能离开团队
        if (userTeam.getIsOwner()) {
            throw new ApiException(ErrorCode.TEAM_OWNER_CANNOT_LEAVE);
        }

        userTeamRepository.deleteById(userTeam.getId());
    }

    @Override
    @Transactional
    public void removeMember(Long removerUserId, Long targetUserId, Long teamId) {
        // 检查执行移除操作的用户是否是团队所有者
        QueryWrapper removerQuery = QueryWrapper.create()
                .where(USER_TEAM_PO.USER_ID.eq(removerUserId))
                .and(USER_TEAM_PO.TEAM_ID.eq(teamId))
                .and(USER_TEAM_PO.IS_OWNER.eq(true));

        UserTeamPO remover = userTeamRepository.selectOneByQuery(removerQuery);
        if (remover == null) {
            throw new ApiException(ErrorCode.ONLY_TEAM_OWNER_CAN_REMOVE_MEMBER);
        }

        // 检查目标用户是否在团队中
        QueryWrapper targetQuery = QueryWrapper.create()
                .where(USER_TEAM_PO.USER_ID.eq(targetUserId))
                .and(USER_TEAM_PO.TEAM_ID.eq(teamId));

        UserTeamPO targetUserTeam = userTeamRepository.selectOneByQuery(targetQuery);
        if (targetUserTeam == null) {
            throw new ApiException(ErrorCode.TARGET_USER_NOT_IN_TEAM);
        }

        // 团队所有者不能移除自己
        if (targetUserId.equals(removerUserId)) {
            throw new ApiException(ErrorCode.TEAM_OWNER_CANNOT_REMOVE_SELF);
        }

        // 如果目标用户是团队所有者，不能移除（虽然正常情况下所有者只有一个）
        if (targetUserTeam.getIsOwner()) {
            throw new ApiException(ErrorCode.CANNOT_REMOVE_TEAM_OWNER);
        }

        userTeamRepository.deleteById(targetUserTeam.getId());
    }

    private UserTeamDTO convertToDto(UserTeamPO userTeam) {
        // 获取用户信息
        UserPO user = userRepository.selectOneById(userTeam.getUserId());
        // 获取团队信息
        TeamPO team = teamRepository.selectOneById(userTeam.getTeamId());

        return UserTeamDTO.builder()
                .id(userTeam.getId())
                .userId(userTeam.getUserId())
                .teamId(userTeam.getTeamId())
                .isOwner(userTeam.getIsOwner())
                .createdAt(userTeam.getCreatedAt())
                .updatedAt(userTeam.getUpdatedAt())
                .userName(user != null ? user.getUsername() : null)
                .userRealName(user != null ? user.getRealName() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .teamName(team != null ? team.getName() : null)
                .teamColor(team != null ? team.getColor() : null)
                .build();
    }
}
