package com.shuttleshout.service.impl;

import static com.shuttleshout.common.model.po.table.PlayerTableDef.PLAYER;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import com.shuttleshout.common.exception.ApiException;
import com.shuttleshout.common.exception.ErrorCode;
import com.shuttleshout.common.model.dto.PlayerDTO;
import com.shuttleshout.common.model.po.Player;
import com.shuttleshout.common.model.po.UserPO;
import com.shuttleshout.repository.PlayerRepository;
import com.shuttleshout.repository.UserRepository;
import com.shuttleshout.service.PlayerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 球員服務實現類
 * 
 * @author ShuttleShout Team
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PlayerServiceImpl extends ServiceImpl<PlayerRepository, Player> implements PlayerService {

    private final UserRepository userRepository;

    @Override
    public List<PlayerDTO> getAllPlayers() {
        List<Player> players = getMapper().selectAll();
        return players.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PlayerDTO getPlayerById(Long id) {
        Player player = getMapper().selectOneById(id);
        if (player == null) {
            throw new ApiException(ErrorCode.PLAYER_NOT_FOUND, "球員不存在，ID: " + id);
        }
        return convertToDto(player);
    }

    @Override
    public List<PlayerDTO> getPlayersByTeamId(Long teamId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(PLAYER.TEAM_ID.eq(teamId));
        List<Player> players = getMapper().selectListByQuery(queryWrapper);
        return players.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Player createPlayerFromUser(Long userId, Long teamId) {
        // 獲取用戶信息
        UserPO user = userRepository.selectOneById(userId);
        if (user == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, "用戶不存在，ID: " + userId);
        }

        // 檢查是否已經存在該用戶在該團隊的球員記錄
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(PLAYER.TEAM_ID.eq(teamId))
                .and(PLAYER.NAME.eq(user.getRealName() != null ? user.getRealName() : user.getUsername()));
        Player existingPlayer = getMapper().selectOneByQuery(queryWrapper);
        
        if (existingPlayer != null) {
            log.info("球員已存在，返回現有記錄: playerId={}, userId={}, teamId={}", 
                    existingPlayer.getId(), userId, teamId);
            return existingPlayer;
        }

        // 創建新的球員記錄
        LocalDateTime now = LocalDateTime.now();
        Player player = new Player();
        player.setName(user.getRealName() != null ? user.getRealName() : user.getUsername());
        player.setPhoneNumber(user.getPhoneNumber());
        player.setNotes("從用戶自動創建");
        player.setTeamId(teamId);
        player.setCreatedAt(now);
        player.setUpdatedAt(now);

        getMapper().insert(player);
        log.info("成功創建球員記錄: playerId={}, userId={}, teamId={}, name={}", 
                player.getId(), userId, teamId, player.getName());
        
        return player;
    }

    @Override
    public PlayerDTO createPlayer(Player player) {
        LocalDateTime now = LocalDateTime.now();
        player.setCreatedAt(now);
        player.setUpdatedAt(now);
        getMapper().insert(player);
        return convertToDto(player);
    }

    @Override
    public PlayerDTO updatePlayer(Long id, Player player) {
        Player existingPlayer = getMapper().selectOneById(id);
        if (existingPlayer == null) {
            throw new ApiException(ErrorCode.PLAYER_NOT_FOUND, "球員不存在，ID: " + id);
        }

        if (player.getName() != null) {
            existingPlayer.setName(player.getName());
        }
        if (player.getPhoneNumber() != null) {
            existingPlayer.setPhoneNumber(player.getPhoneNumber());
        }
        if (player.getNotes() != null) {
            existingPlayer.setNotes(player.getNotes());
        }
        existingPlayer.setUpdatedAt(LocalDateTime.now());

        getMapper().update(existingPlayer);
        return convertToDto(existingPlayer);
    }

    @Override
    public void deletePlayer(Long id) {
        Player player = getMapper().selectOneById(id);
        if (player == null) {
            throw new ApiException(ErrorCode.PLAYER_NOT_FOUND, "球員不存在，ID: " + id);
        }
        getMapper().deleteById(id);
    }

    /**
     * 轉換為DTO
     */
    private PlayerDTO convertToDto(Player player) {
        PlayerDTO dto = new PlayerDTO();
        dto.setId(player.getId());
        dto.setName(player.getName());
        dto.setPhoneNumber(player.getPhoneNumber());
        dto.setNotes(player.getNotes());
        dto.setTeamId(player.getTeamId());
        dto.setCreatedAt(player.getCreatedAt());
        dto.setUpdatedAt(player.getUpdatedAt());
        return dto;
    }
}

