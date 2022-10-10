package com.xd.pre.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd.pre.modules.sys.domain.SysRoleDept;

import java.util.List;

/**
 * <p>
 * 角色与部门对应关系 服务类
 * </p>
 *
 * @author lihaodong
 * @since 2019-04-21
 */
public interface ISysRoleDeptService extends IService<SysRoleDept> {

    /**
     * 根据角色id查询部门ids
     * @param roleId
     * @return
     */
    List<SysRoleDept> getRoleDeptIds(int roleId);
}
