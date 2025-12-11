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
import org.springframework.http.HttpStatus;

import com.shuttleshout.common.exception.ApiException;
import com.shuttleshout.common.model.dto.UserCreateDTO;
import com.shuttleshout.common.model.dto.UserDTO;
import com.shuttleshout.common.model.dto.UserUpdateDTO;
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
     * 获取所有用户
     */
    @Override
    public List<UserDTO> getAllUsers() {
        List<UserPO> users = getMapper().selectAll();
        return users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取用户
     */
    @Override
    public UserDTO getUserById(Long id) {
        UserPO user = getMapper().selectOneById(id);
        if (user == null) {
            throw new ApiException("用户不存在，ID: " + id, HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
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
            throw new ApiException("用户不存在，用户名: " + username, HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
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
            throw new ApiException("用户名已存在: " + userCreateDto.getUsername(), HttpStatus.BAD_REQUEST, "USERNAME_ALREADY_EXISTS");
        }

        // 检查邮箱是否已存在
        if (userCreateDto.getEmail() != null && !userCreateDto.getEmail().isEmpty()) {
            queryWrapper = QueryWrapper.create()
                    .where(USER_PO.EMAIL.eq(userCreateDto.getEmail()));
            existingUser = getMapper().selectOneByQuery(queryWrapper);
            if (existingUser != null) {
                throw new ApiException("邮箱已存在: " + userCreateDto.getEmail(), HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_EXISTS");
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
            throw new ApiException("用户不存在，ID: " + id, HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
        }

        // 检查邮箱是否已被其他用户使用
        if (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().equals(user.getEmail())) {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .where(USER_PO.EMAIL.eq(userUpdateDto.getEmail()))
                    .and(USER_PO.ID.ne(id));
            UserPO existingUser = getMapper().selectOneByQuery(queryWrapper);
            if (existingUser != null) {
                throw new ApiException("邮箱已被使用: " + userUpdateDto.getEmail(), HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_EXISTS");
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
            throw new ApiException("用户不存在，ID: " + id, HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
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
                throw new ApiException("角色不存在，ID: " + roleId, HttpStatus.NOT_FOUND, "ROLE_NOT_FOUND");
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
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        // 获取用户角色名称列表
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            List<String> roleNames = user.getRoles().stream()
                    .map(RolePO::getName)
                    .collect(Collectors.toList());
            dto.setRoleNames(roleNames);
        }

        return dto;
    }
}

