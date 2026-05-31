package com.forest.access.role.repository;

import com.forest.access.role.entity.AccessRolePermissionPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccessRolePermissionRepository extends JpaRepository<AccessRolePermissionPO, Long> {
    Optional<AccessRolePermissionPO> findByRoleIdAndPermissionPattern(Long roleId, String permissionPattern);

    List<AccessRolePermissionPO> findByRoleIdAndDeleted(Long roleId, Integer deleted);

    List<AccessRolePermissionPO> findByRoleId(Long roleId);

    List<AccessRolePermissionPO> findByRoleIdInAndDeleted(List<Long> roleIds, Integer deleted);
}
