package com.shuttleshout.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shuttleshout.common.annotation.CurrentUserId;
import com.shuttleshout.common.exception.ApiException;
import com.shuttleshout.common.model.dto.UserTeamCreateDTO;
import com.shuttleshout.common.model.dto.UserTeamDTO;
import com.shuttleshout.service.UserTeamService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 用户团队关系控制器
 *
 * @author ShuttleShout Team
 */
@RestController
@RequestMapping("/user-teams")
@Tag(name = "用户团队管理", description = "用户加入/离开团队相关的API接口")
public class UserTeamController {

    @Autowired
    private UserTeamService userTeamService;

    /**
     * 获取所有用户团队关系
     */
    @GetMapping
    @Operation(summary = "获取所有用户团队关系", description = "返回系统中所有用户团队关系的列表")
    public ResponseEntity<List<UserTeamDTO>> getAllUserTeams() {
        try {
            List<UserTeamDTO> userTeams = userTeamService.getAllUserTeams();
            return ResponseEntity.ok(userTeams);
        } catch (Exception e) {
            throw new ApiException("获取用户团队关系列表失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, "GET_USER_TEAMS_ERROR", e);
        }
    }

    /**
     * 根据团队ID获取团队成员
     */
    @GetMapping("/team/{teamId}")
    @Operation(summary = "根据团队ID获取团队成员", description = "返回指定团队的所有成员")
    public ResponseEntity<List<UserTeamDTO>> getTeamMembers(@PathVariable Long teamId) {
        try {
            List<UserTeamDTO> members = userTeamService.getTeamMembers(teamId);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            throw new ApiException("获取团队成员失败: " + e.getMessage(), HttpStatus.BAD_REQUEST, "GET_TEAM_MEMBERS_ERROR", e);
        }
    }

    /**
     * 根据用户ID获取用户加入的团队
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID获取用户加入的团队", description = "返回指定用户加入的所有团队")
    public ResponseEntity<List<UserTeamDTO>> getUserTeams(@PathVariable Long userId) {
        try {
            List<UserTeamDTO> userTeams = userTeamService.getUserTeams(userId);
            return ResponseEntity.ok(userTeams);
        } catch (Exception e) {
            throw new ApiException("获取用户团队失败: " + e.getMessage(), HttpStatus.BAD_REQUEST, "GET_USER_TEAMS_ERROR", e);
        }
    }

    /**
     * 用户加入团队
     */
    @PostMapping
    @Operation(summary = "用户加入团队", description = "将指定用户加入指定团队")
    public ResponseEntity<UserTeamDTO> joinTeam(@Valid @RequestBody UserTeamCreateDTO userTeamCreateDto) {
        try {
            UserTeamDTO userTeam = userTeamService.joinTeam(userTeamCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(userTeam);
        } catch (Exception e) {
            throw new ApiException("加入团队失败: " + e.getMessage(), HttpStatus.BAD_REQUEST, "JOIN_TEAM_ERROR", e);
        }
    }

    /**
     * 用户离开团队
     */
    @DeleteMapping("/{userId}/{teamId}")
    @Operation(summary = "用户离开团队", description = "将指定用户从指定团队中移除")
    public ResponseEntity<Void> leaveTeam(@PathVariable Long userId, @PathVariable Long teamId) {
        try {
            userTeamService.leaveTeam(userId, teamId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ApiException("离开团队失败: " + e.getMessage(), HttpStatus.BAD_REQUEST, "LEAVE_TEAM_ERROR", e);
        }
    }

    /**
     * 当前用户加入团队
     */
    @PostMapping("/join/{teamId}")
    @Operation(summary = "当前用户加入团队", description = "当前登录用户加入指定团队")
    public ResponseEntity<UserTeamDTO> currentUserJoinTeam(@PathVariable Long teamId, @CurrentUserId Long currentUserId) {
        try {
            UserTeamCreateDTO dto = new UserTeamCreateDTO();
            dto.setUserId(currentUserId);
            dto.setTeamId(teamId);
            dto.setIsOwner(false);

            UserTeamDTO userTeam = userTeamService.joinTeam(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(userTeam);
        } catch (Exception e) {
            throw new ApiException("加入团队失败: " + e.getMessage(), HttpStatus.BAD_REQUEST, "JOIN_TEAM_ERROR", e);
        }
    }

    /**
     * 当前用户离开团队
     */
    @DeleteMapping("/leave/{teamId}")
    @Operation(summary = "当前用户离开团队", description = "当前登录用户离开指定团队")
    public ResponseEntity<Void> currentUserLeaveTeam(@PathVariable Long teamId, @CurrentUserId Long currentUserId) {
        try {
            userTeamService.leaveTeam(currentUserId, teamId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ApiException("离开团队失败: " + e.getMessage(), HttpStatus.BAD_REQUEST, "LEAVE_TEAM_ERROR", e);
        }
    }

    /**
     * 团队所有者移除成员
     */
    @DeleteMapping("/remove/{targetUserId}/{teamId}")
    @Operation(summary = "团队所有者移除成员", description = "团队所有者从团队中移除指定成员（不能移除自己）")
    public ResponseEntity<Void> removeMember(@PathVariable Long targetUserId, @PathVariable Long teamId, @CurrentUserId Long currentUserId) {
        try {
            userTeamService.removeMember(currentUserId, targetUserId, teamId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ApiException("移除成员失败: " + e.getMessage(), HttpStatus.BAD_REQUEST, "REMOVE_MEMBER_ERROR", e);
        }
    }
}
