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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shuttleshout.common.annotation.CurrentUserId;
import com.shuttleshout.common.exception.ApiException;
import com.shuttleshout.common.model.dto.TeamCreateDTO;
import com.shuttleshout.common.model.dto.TeamDTO;
import com.shuttleshout.common.model.dto.TeamUpdateDTO;
import com.shuttleshout.service.TeamService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 球隊控制器
 * 
 * @author ShuttleShout Team
 */
@RestController
@RequestMapping("/teams")
@Tag(name = "球隊管理", description = "球隊相關的API接口")
public class TeamController {

    @Autowired
    private TeamService teamService;

    /**
     * 獲取所有球隊
     */
    @GetMapping
    @Operation(summary = "獲取所有球隊", description = "返回系統中所有球隊的列表")
    public ResponseEntity<List<TeamDTO>> getAllTeams() {
        try {
            List<TeamDTO> teams = teamService.getAllTeams();
            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            throw new ApiException("獲取球隊列表失敗: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, "GET_TEAMS_ERROR", e);
        }
    }

    /**
     * 獲取當前登錄用戶創建的球隊
     */
    @GetMapping("/my")
    @Operation(summary = "獲取當前用戶創建的球隊", description = "根據JWT token獲取當前登錄用戶創建的所有球隊")
    public ResponseEntity<List<TeamDTO>> getMyTeams(@CurrentUserId Long userId) {
        try {
            List<TeamDTO> teams = teamService.getTeamsByUserId(userId);
            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            throw new ApiException("獲取我的球隊列表失敗: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, "GET_MY_TEAMS_ERROR", e);
        }
    }

    /**
     * 根據ID獲取球隊
     */
    @GetMapping("/{id}")
    @Operation(summary = "根據ID獲取球隊", description = "根據球隊ID獲取球隊詳細資訊")
    public ResponseEntity<TeamDTO> getTeamById(@PathVariable Long id) {
        try {
            TeamDTO team = teamService.getTeamById(id);
            return ResponseEntity.ok(team);
        } catch (Exception e) {
            throw new ApiException("獲取球隊失敗: " + e.getMessage(), HttpStatus.BAD_REQUEST, "GET_TEAM_ERROR", e);
        }
    }

    /**
     * 根據名稱獲取球隊
     */
    @GetMapping("/name/{name}")
    @Operation(summary = "根據名稱獲取球隊", description = "根據球隊名稱獲取球隊詳細資訊")
    public ResponseEntity<TeamDTO> getTeamByName(@PathVariable String name) {
        try {
            TeamDTO team = teamService.getTeamByName(name);
            return ResponseEntity.ok(team);
        } catch (Exception e) {
            throw new ApiException("獲取球隊失敗: " + e.getMessage(), HttpStatus.BAD_REQUEST, "GET_TEAM_ERROR", e);
        }
    }

    /**
     * 創建球隊
     */
    @PostMapping
    @Operation(summary = "創建球隊", description = "創建新的球隊")
    public ResponseEntity<TeamDTO> createTeam(@Valid @RequestBody TeamCreateDTO teamCreateDto) {
        try {
            TeamDTO createdTeam = teamService.createTeam(teamCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTeam);
        } catch (Exception e) {
            throw new ApiException("創建球隊失敗: " + e.getMessage(), HttpStatus.BAD_REQUEST, "CREATE_TEAM_ERROR", e);
        }
    }

    /**
     * 更新球隊
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新球隊", description = "更新指定球隊的資訊")
    public ResponseEntity<TeamDTO> updateTeam(@PathVariable Long id, @Valid @RequestBody TeamUpdateDTO teamUpdateDto) {
        try {
            TeamDTO updatedTeam = teamService.updateTeam(id, teamUpdateDto);
            return ResponseEntity.ok(updatedTeam);
        } catch (Exception e) {
            throw new ApiException("更新球隊失敗: " + e.getMessage(), HttpStatus.BAD_REQUEST, "UPDATE_TEAM_ERROR", e);
        }
    }

    /**
     * 刪除球隊
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除球隊", description = "刪除指定的球隊")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        try {
            teamService.deleteTeam(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ApiException("刪除球隊失敗: " + e.getMessage(), HttpStatus.BAD_REQUEST, "DELETE_TEAM_ERROR", e);
        }
    }
}

