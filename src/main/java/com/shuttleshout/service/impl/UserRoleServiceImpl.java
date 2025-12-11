package com.shuttleshout.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.shuttleshout.common.model.po.UserRolePO;
import com.shuttleshout.repository.UserRoleRepository;
import com.shuttleshout.service.UserRoleService;

public class UserRoleServiceImpl extends ServiceImpl<UserRoleRepository, UserRolePO> implements UserRoleService {

    @Override
    public List<UserRolePO> getAllUserRoles() {
        return getMapper().selectAll();
    }   
    
}
