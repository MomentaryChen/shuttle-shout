package com.shuttleshout.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shuttleshout.common.model.po.Court;
import com.shuttleshout.service.CourtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 場地控制器
 * 
 * @author ShuttleShout Team
 */
@RestController
@RequestMapping("/api/courts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "場地管理", description = "場地相關的API接口")
public class CourtController {

    private final CourtService courtService;

    @GetMapping("/team/{teamId}")
    @Operation(summary = "根據團隊ID獲取場地列表", description = "獲取指定團隊的所有場地")
    public ResponseEntity<List<Court>> getCourtsByTeamId(
            @Parameter(description = "團隊ID") @PathVariable Long teamId) {
        log.info("獲取團隊 {} 的場地列表", teamId);
        List<Court> courts = courtService.getCourtsByTeamId(teamId);
        return ResponseEntity.ok(courts);
    }

    @PostMapping("/team/{teamId}/initialize")
    @Operation(summary = "初始化團隊場地", description = "自動為團隊創建所需數量的場地")
    public ResponseEntity<List<Court>> initializeCourtsForTeam(
            @Parameter(description = "團隊ID") @PathVariable Long teamId) {
        log.info("初始化團隊 {} 的場地", teamId);
        List<Court> courts = courtService.initializeCourtsForTeam(teamId);
        return ResponseEntity.ok(courts);
    }

    @PostMapping
    @Operation(summary = "創建場地", description = "創建一個新場地")
    public ResponseEntity<Court> createCourt(
            @Valid @RequestBody Court court) {
        log.info("創建場地: {}", court.getName());
        Court createdCourt = courtService.createCourt(court);
        return ResponseEntity.ok(createdCourt);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根據ID獲取場地", description = "獲取指定ID的場地詳情")
    public ResponseEntity<Court> getCourtById(
            @Parameter(description = "場地ID") @PathVariable Long id) {
        log.info("獲取場地詳情, ID: {}", id);
        Court court = courtService.getCourtById(id);
        return ResponseEntity.ok(court);
    }

    @GetMapping("/{courtId}/players")
    @Operation(summary = "獲取場地球員", description = "獲取場地上正在進行的比賽的球員列表")
    public ResponseEntity<List<Long>> getPlayersOnCourt(
            @Parameter(description = "場地ID") @PathVariable Long courtId) {
        log.info("獲取場地 {} 的球員列表", courtId);
        List<Long> players = courtService.getPlayersOnCourt(courtId);
        return ResponseEntity.ok(players);
    }
}

