package com.myfschool.service;

import com.myfschool.entity.Semester;
import com.myfschool.repository.SemesterRepository;
import org.springframework.stereotype.Service;

@Service
public class SemesterService extends AbstractCrudService<Semester> {

    public SemesterService(SemesterRepository repository) {
        super(repository, "Semester");
    }
}
