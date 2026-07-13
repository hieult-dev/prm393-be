package com.myfschool.service;

import com.myfschool.entity.Permission;
import com.myfschool.repository.PermissionRepository;
import org.springframework.stereotype.Service;

@Service
public class PermissionService extends AbstractCrudService<Permission> {

    public PermissionService(PermissionRepository repository) {
        super(repository, "Permission");
    }
}
