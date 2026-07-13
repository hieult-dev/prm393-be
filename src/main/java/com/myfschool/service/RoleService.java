package com.myfschool.service;

import com.myfschool.entity.Permission;
import com.myfschool.entity.Role;
import com.myfschool.exception.ResourceNotFoundException;
import com.myfschool.repository.PermissionRepository;
import com.myfschool.repository.RoleRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService extends AbstractCrudService<Role> {

    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository repository, PermissionRepository permissionRepository) {
        super(repository, "Role");
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional
    public Role create(Role role) {
        role.setRoleName(normalizeName(role.getRoleName()));
        role.setPermissions(resolvePermissions(role));
        return super.create(role);
    }

    @Override
    @Transactional
    public Role update(Long id, Role role) {
        role.setRoleName(normalizeName(role.getRoleName()));
        role.setPermissions(resolvePermissions(role));
        return super.update(id, role);
    }

    private Set<Permission> resolvePermissions(Role role) {
        Set<Permission> permissions = new LinkedHashSet<>();

        if (role.getPermissions() == null) {
            return permissions;
        }

        role.getPermissions().forEach(permission -> permissions.add(resolvePermission(permission)));
        return permissions;
    }

    private Permission resolvePermission(Permission permission) {
        if (permission.getId() != null) {
            return permissionRepository.findById(permission.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Permission", permission.getId()));
        }

        if (permission.getPermissionName() == null || permission.getPermissionName().isBlank()) {
            throw new ResourceNotFoundException("Permission", "permissionName", permission.getPermissionName());
        }

        String permissionName = normalizeName(permission.getPermissionName());
        return permissionRepository.findByPermissionName(permissionName)
                .orElseGet(() -> {
                    Permission newPermission = new Permission();
                    newPermission.setPermissionName(permissionName);
                    newPermission.setDescription(permission.getDescription());
                    return permissionRepository.save(newPermission);
                });
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim().toUpperCase();
    }
}
