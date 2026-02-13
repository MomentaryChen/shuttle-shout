package com.shuttleshout.service.impl;

import static com.shuttleshout.common.model.po.table.UserPOTableDef.USER_PO;
import static com.shuttleshout.common.model.po.table.UserRolePOTableDef.USER_ROLE_PO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import com.shuttleshout.common.exception.ApiException;
import com.shuttleshout.common.exception.ErrorCode;
import com.shuttleshout.common.model.dto.UserCreateDTO;
import com.shuttleshout.common.model.dto.UserDTO;
import com.shuttleshout.common.model.dto.UserUpdateDTO;
import com.shuttleshout.common.model.enums.BadmintonLevel;
import com.shuttleshout.common.model.po.RolePO;
import com.shuttleshout.common.model.po.UserPO;
import com.shuttleshout.common.model.po.UserRolePO;
import com.shuttleshout.common.util.PasswordUtil;
import com.shuttleshout.repository.RoleRepository;
import com.shuttleshout.repository.UserRepository;
import com.shuttleshout.repository.UserRoleRepository;
import com.shuttleshout.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * 用户服务实现类
 * 
 * @author ShuttleShout Team
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserRepository, UserPO> implements UserService {

    private final UserRoleRepository userRoleRepository;

    private final RoleRepository roleRepository;

    private final PasswordUtil passwordUtil;

    /**
     * 獲取所有用戶（含角色關聯，以便 DTO 回傳 roleNames / roleCodes 給前端）
     */
    @Override
    public List<UserDTO> getAllUsers() {
        List<UserPO> users = getMapper().selectAll();
        return users.stream()
                .map(user -> {
                    UserPO withRelations = getMapper().selectOneWithRelationsById(user.getId());
                    return convertToDto(withRelations != null ? withRelations : user);
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取用户
     */
    @Override
    public UserDTO getUserById(Long id) {
        UserPO user = getMapper().selectOneById(id);
        if (user == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, "用戶不存在，ID: " + id);
        }
        return convertToDto(user);
    }

    /**
     * 根据用户名获取用户
     */
    @Override
    public UserDTO getUserByUsername(String username) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(USER_PO.USERNAME.eq(username));
        UserPO user = getMapper().selectOneByQuery(queryWrapper);
        if (user == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, "用戶不存在，用戶名: " + username);
        }
        return convertToDto(user);
    }

    /**
     * 创建用户
     */
    @Override
    public UserDTO createUser(@Valid UserCreateDTO userCreateDto) {
        // 检查用户名是否已存在
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(USER_PO.USERNAME.eq(userCreateDto.getUsername()));
        UserPO existingUser = getMapper().selectOneByQuery(queryWrapper);
        if (existingUser != null) {
            throw new ApiException(ErrorCode.USERNAME_ALREADY_EXISTS, "用戶名已存在: " + userCreateDto.getUsername());
        }

        // 检查邮箱是否已存在
        if (userCreateDto.getEmail() != null && !userCreateDto.getEmail().isEmpty()) {
            queryWrapper = QueryWrapper.create()
                    .where(USER_PO.EMAIL.eq(userCreateDto.getEmail()));
            existingUser = getMapper().selectOneByQuery(queryWrapper);
            if (existingUser != null) {
                throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS, "郵箱已存在: " + userCreateDto.getEmail());
            }
        }

        UserPO user = new UserPO();
        user.setUsername(userCreateDto.getUsername());
        // 加密密码
        user.setPassword(passwordUtil.encode(userCreateDto.getPassword()));
        user.setEmail(userCreateDto.getEmail());
        user.setPhoneNumber(userCreateDto.getPhoneNumber());
        user.setRealName(userCreateDto.getRealName());
        user.setIsActive(true);
        if (userCreateDto.getBadmintonLevel() != null && !BadmintonLevel.isValid(userCreateDto.getBadmintonLevel())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "羽球等級須為 1 至 18 或未設定");
        }
        user.setBadmintonLevel(userCreateDto.getBadmintonLevel());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        getMapper().insert(user);

        // 分配角色
        if (userCreateDto.getRoleIds() != null && !userCreateDto.getRoleIds().isEmpty()) {
            assignRolesToUser(user.getId(), userCreateDto.getRoleIds());
        }

        return convertToDto(user);
    }

    /**
     * 更新用户
     */
    @Override
    public UserDTO updateUser(Long id, @Valid UserUpdateDTO userUpdateDto) {
        UserPO user = getMapper().selectOneById(id);
        if (user == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, "用戶不存在，ID: " + id);
        }

        // 检查邮箱是否已被其他用户使用
        if (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().equals(user.getEmail())) {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .where(USER_PO.EMAIL.eq(userUpdateDto.getEmail()))
                    .and(USER_PO.ID.ne(id));
            UserPO existingUser = getMapper().selectOneByQuery(queryWrapper);
            if (existingUser != null) {
                throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS, "郵箱已被使用: " + userUpdateDto.getEmail());
            }
        }

        if (userUpdateDto.getEmail() != null) {
            user.setEmail(userUpdateDto.getEmail());
        }
        if (userUpdateDto.getPhoneNumber() != null) {
            user.setPhoneNumber(userUpdateDto.getPhoneNumber());
        }
        if (userUpdateDto.getRealName() != null) {
            user.setRealName(userUpdateDto.getRealName());
        }
        if (userUpdateDto.getAvatar() != null) {
            user.setAvatar(userUpdateDto.getAvatar());
        }
        if (userUpdateDto.getIsActive() != null) {
            user.setIsActive(userUpdateDto.getIsActive());
        }
        if (userUpdateDto.getBadmintonLevel() != null && !BadmintonLevel.isValid(userUpdateDto.getBadmintonLevel())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "羽球等級須為 1 至 18 或未設定");
        }
        user.setBadmintonLevel(userUpdateDto.getBadmintonLevel());
        if (userUpdateDto.getPassword() != null && !userUpdateDto.getPassword().isEmpty()) {
            // 加密密码
            user.setPassword(passwordUtil.encode(userUpdateDto.getPassword()));
        }
        user.setUpdatedAt(LocalDateTime.now());

        getMapper().update(user);

        // 更新角色
        if (userUpdateDto.getRoleIds() != null) {
            assignRolesToUser(user.getId(), userUpdateDto.getRoleIds());
        }

        return convertToDto(user);
    }

    /**
     * 删除用户
     */
    @Override
    public void deleteUser(Long id) {
        UserPO user = getMapper().selectOneById(id);
        if (user == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, "用戶不存在，ID: " + id);
        }

        // 删除用户角色关联
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(USER_ROLE_PO.USER_ID.eq(id));
        List<UserRolePO> userRoles = userRoleRepository.selectListByQuery(queryWrapper);
        for (UserRolePO userRole : userRoles) {
            getMapper().deleteById(userRole.getId());
        }

        getMapper().deleteById(id);
    }

    /**
     * 为用户分配角色
     */
    @Override
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        // 先删除现有角色
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(USER_ROLE_PO.USER_ID.eq(userId));
        List<UserRolePO> existingUserRoles = userRoleRepository.selectListByQuery(queryWrapper);
        for (UserRolePO userRole : existingUserRoles) {
            userRoleRepository.deleteById(userRole.getId());
        }

        // 添加新角色
        for (Long roleId : roleIds) {
            RolePO role = roleRepository.selectOneById(roleId);
            if (role == null) {
                throw new ApiException(ErrorCode.ROLE_NOT_FOUND, "角色不存在，ID: " + roleId);
            }

            UserRolePO userRole = new UserRolePO();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setCreatedAt(LocalDateTime.now());
            userRoleRepository.insert(userRole);
        }

    }

    /**
     * 转换为DTO
     */
    private UserDTO convertToDto(UserPO user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRealName(user.getRealName());
        dto.setAvatar(user.getAvatar());
        dto.setIsActive(user.getIsActive());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setBadmintonLevel(user.getBadmintonLevel());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        // 取得使用者角色名稱與代碼列表（代碼供前端辨識 SYSTEM_ADMIN 等，統計一致）
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            List<String> roleNames = user.getRoles().stream()
                    .map(RolePO::getName)
                    .collect(Collectors.toList());
            dto.setRoleNames(roleNames);
            List<String> roleCodes = user.getRoles().stream()
                    .map(RolePO::getCode)
                    .filter(code -> code != null && !code.isEmpty())
                    .collect(Collectors.toList());
            dto.setRoleCodes(roleCodes);
        }

        return dto;
    }
}

