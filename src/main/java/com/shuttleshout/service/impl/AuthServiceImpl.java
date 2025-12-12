package com.shuttleshout.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.shuttleshout.common.model.po.table.RoleResourcePagePOTableDef.ROLE_RESOURCE_PAGE_PO;

import com.mybatisflex.core.query.QueryWrapper;
import com.shuttleshout.common.exception.ApiException;
import com.shuttleshout.common.exception.ErrorCode;
import com.shuttleshout.common.model.dto.LoginRequest;
import com.shuttleshout.common.model.dto.LoginResponse;
import com.shuttleshout.common.model.dto.RegisterRequest;
import com.shuttleshout.common.model.dto.ResourcePageCreateDTO;
import com.shuttleshout.common.model.dto.ResourcePageDTO;
import com.shuttleshout.common.model.dto.RoleDTO;
import com.shuttleshout.common.model.dto.UserCreateDTO;
import com.shuttleshout.common.model.dto.UserDTO;
import com.shuttleshout.common.model.po.RoleResourcePagePO;
import com.shuttleshout.common.model.po.UserPO;
import com.shuttleshout.common.util.JwtUtil;
import com.shuttleshout.repository.RoleResourcePageRepository;
import com.shuttleshout.repository.UserRepository;
import com.shuttleshout.service.AuthService;
import com.shuttleshout.service.ResourcePageService;
import com.shuttleshout.service.RoleService;
import com.shuttleshout.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证服务实现类
 *
 * @author ShuttleShout Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserService userService;
    private final RoleService roleService;
    private final ResourcePageService resourcePageService;
    private final RoleResourcePageRepository roleResourcePageRepository;

    /**
     * 用户注册
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDTO register(@Valid RegisterRequest registerRequest) {
        // 确保 PLAYER 角色存在
        RoleDTO playerRole = ensurePlayerRoleExists();
        
        // 确保团队总览资源页面存在并分配给 PLAYER 角色
        ensureTeamOverviewPageExists(playerRole.getId());

        // 将 RegisterRequest 转换为 UserCreateDTO
        UserCreateDTO userCreateDto = new UserCreateDTO();
        userCreateDto.setUsername(registerRequest.getUsername());
        userCreateDto.setPassword(registerRequest.getPassword());
        userCreateDto.setEmail(registerRequest.getEmail());
        userCreateDto.setPhoneNumber(registerRequest.getPhoneNumber());
        userCreateDto.setRealName(registerRequest.getRealName());
        // 注册时自动分配 PLAYER 角色
        userCreateDto.setRoleIds(Arrays.asList(playerRole.getId()));

        // 创建用户
        UserDTO createdUser = userService.createUser(userCreateDto);
        log.info("用戶註冊成功，用戶名: {}, 用戶ID: {}, 已分配 PLAYER 角色", 
                createdUser.getUsername(), createdUser.getId());

        // 返回注册成功的用户信息（不再自动登录）
        return createdUser;
    }

    /**
     * 用户登录
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            // 先通过用户名查找用户，获取用户ID
            UserDTO userDto = userService.getUserByUsername(loginRequest.getUsername());
            if (userDto == null || userDto.getId() == null) {
                throw new ApiException(ErrorCode.USER_NOT_FOUND, "用戶不存在");
            }

            // 使用用户ID进行认证（因为CustomUserDetailsService使用ID作为username）
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            String.valueOf(userDto.getId()),
                            loginRequest.getPassword()
                    )
            );

            // 获取用户详情
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 生成JWT token
            String token = jwtUtil.generateToken(userDetails);

            // 更新最后登录时间
            UserPO user = userRepository.selectOneById(userDto.getId());
            if (user != null) {
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.update(user);
            }

            // 构建响应
            return LoginResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .user(userDto)
                    .build();

        } catch (BadCredentialsException e) {
            throw new ApiException(ErrorCode.BAD_CREDENTIALS);
        } catch (ApiException e) {
            // 如果已经是 ApiException，直接抛出
            throw e;
        } catch (Exception e) {
            // 直接抛出原始异常，让 Controller 层统一处理异常消息格式
            throw e;
        }
    }

    /**
     * 用户登出
     */
    @Override
    public void logout(String token) {
        // 记录登出日志
        log.info("用戶登出，token: {}", token != null && token.length() > 10 ?
                 token.substring(0, 10) + "..." : token);
    }

    /**
     * 确保 PLAYER 角色存在，如果不存在则创建
     */
    private RoleDTO ensurePlayerRoleExists() {
        try {
            // 尝试获取 PLAYER 角色
            return roleService.getRoleByCode("PLAYER");
        } catch (ApiException e) {
            // 如果角色不存在，创建它
            if ("ROLE_NOT_FOUND".equals(e.getErrorCode())) {
                log.info("PLAYER 角色不存在，正在創建...");
                RoleDTO playerRoleDto = new RoleDTO();
                playerRoleDto.setName("球員");
                playerRoleDto.setCode("PLAYER");
                playerRoleDto.setDescription("默认球員角色，所有注册用户自动获得此角色");
                playerRoleDto.setIsActive(true);
                try {
                    return roleService.createRole(playerRoleDto);
                } catch (ApiException createException) {
                    // 如果创建失败（可能是并发创建导致代码已存在），再次尝试获取
                    if ("ROLE_CODE_ALREADY_EXISTS".equals(createException.getErrorCode())) {
                        log.info("PLAYER 角色已被其他線程創建，重新獲取...");
                        return roleService.getRoleByCode("PLAYER");
                    }
                    throw createException;
                }
            }
            throw e;
        }
    }

    /**
     * 确保团队总览资源页面存在，如果不存在则创建并分配给 PLAYER 角色
     */
    private void ensureTeamOverviewPageExists(Long playerRoleId) throws ApiException {
        // 尝试获取团队总览页面
        ResourcePageDTO teamOverviewPage = resourcePageService.getResourcePageByCode("TEAM_OVERVIEW");
            
        // 检查 PLAYER 角色是否有访问权限
        List<ResourcePageDTO> playerPages = resourcePageService.getResourcePagesByRoleId(playerRoleId);
        boolean hasAccess = playerPages.stream()
                .anyMatch(page -> "TEAM_OVERVIEW".equals(page.getCode()));
        
        if (!hasAccess) {
            // 如果 PLAYER 角色没有访问权限，添加关联（不覆盖现有关联）
            log.info("為 PLAYER 角色分配團隊總覽頁面訪問權限");
            addRoleResourcePageAssociation(playerRoleId, teamOverviewPage.getId());
        }
    }

    /**
     * 添加角色和资源页面的关联（如果关联不存在）
     */
    private void addRoleResourcePageAssociation(Long roleId, Long resourcePageId) {
        // 检查关联是否已存在
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(ROLE_RESOURCE_PAGE_PO.ROLE_ID.eq(roleId))
                .and(ROLE_RESOURCE_PAGE_PO.RESOURCE_PAGE_ID.eq(resourcePageId));
        RoleResourcePagePO existingAssociation = roleResourcePageRepository.selectOneByQuery(queryWrapper);
        
        if (existingAssociation == null) {
            // 如果关联不存在，创建它
            RoleResourcePagePO association = new RoleResourcePagePO();
            association.setRoleId(roleId);
            association.setResourcePageId(resourcePageId);
            association.setCanRead(true);
            association.setCanWrite(false);
            association.setCanDelete(false);
            association.setCreatedAt(LocalDateTime.now());
            association.setUpdatedAt(LocalDateTime.now());
            try {
                roleResourcePageRepository.insert(association);
                log.info("已為角色ID {} 添加資源頁面ID {} 的訪問權限", roleId, resourcePageId);
            } catch (Exception e) {
                // 如果插入失败（可能是并发插入导致唯一键冲突），忽略错误
                // 因为关联可能已经被其他线程创建了
                log.debug("角色ID {} 和資源頁面ID {} 的關聯可能已存在（並發插入）", roleId, resourcePageId);
            }
        } else {
            log.debug("角色ID {} 和資源頁面ID {} 的關聯已存在", roleId, resourcePageId);
        }
    }
}

