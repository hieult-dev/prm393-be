package com.myfschool.service;

import com.myfschool.entity.ApplicationType;
import com.myfschool.repository.ApplicationTypeRepository;
import org.springframework.stereotype.Service;

@Service
public class ApplicationTypeService extends AbstractCrudService<ApplicationType> {

    public ApplicationTypeService(ApplicationTypeRepository repository) {
        super(repository, "Application type");
    }
}
