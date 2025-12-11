package com.shuttleshout.service;

import java.util.List;

import javax.validation.Valid;

import com.shuttleshout.common.model.dto.UserCreateDTO;
import com.shuttleshout.common.model.dto.UserDTO;
import com.shuttleshout.common.model.dto.UserUpdateDTO;

/**
 * 用户服务接口
 * 
 * @author ShuttleShout Team
 */
public interface UserService {

    /**
     * 获取所有用户
     */
    List<UserDTO> getAllUsers();

    /**
     * 根据ID获取用户
     */
    UserDTO getUserById(Long id);

    /**
     * 根据用户名获取用户
     */
    UserDTO getUserByUsername(String username);

    /**
     * 创建用户
     */
    UserDTO createUser(@Valid UserCreateDTO userCreateDto);

    /**
     * 更新用户
     */
    UserDTO updateUser(Long id, @Valid UserUpdateDTO userUpdateDto);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 为用户分配角色
     */
    void assignRolesToUser(Long userId, List<Long> roleIds);
}
