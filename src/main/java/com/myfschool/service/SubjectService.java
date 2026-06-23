package com.myfschool.service;

import com.myfschool.entity.Subject;
import com.myfschool.repository.SubjectRepository;
import org.springframework.stereotype.Service;

@Service
public class SubjectService extends AbstractCrudService<Subject> {

    public SubjectService(SubjectRepository repository) {
        super(repository, "Subject");
    }
}
