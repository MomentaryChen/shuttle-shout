package com.shuttleshout.service;

import java.util.List;

import com.mybatisflex.core.service.IService;
import com.shuttleshout.common.model.po.UserRolePO;

public interface UserRoleService extends IService<UserRolePO> {

    List<UserRolePO> getAllUserRoles();
}
