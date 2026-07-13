package com.myfschool.service;

import com.myfschool.entity.SemesterSubject;
import com.myfschool.repository.SemesterSubjectRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SemesterSubjectService extends AbstractCrudService<SemesterSubject> {

    private final SemesterSubjectRepository repository;

    public SemesterSubjectService(SemesterSubjectRepository repository) {
        super(repository, "Semester subject");
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<SemesterSubject> findBySemesterId(Long semesterId) {
        return repository.findBySemesterIdOrderByStartDateAscIdAsc(semesterId);
    }

    @Transactional(readOnly = true)
    public List<SemesterSubject> findBySubjectId(Long subjectId) {
        return repository.findBySubjectIdOrderByStartDateAscIdAsc(subjectId);
    }

    @Transactional(readOnly = true)
    public List<SemesterSubject> findBySemesterIdAndSubjectId(Long semesterId, Long subjectId) {
        return repository.findBySemesterIdAndSubjectIdOrderByStartDateAscIdAsc(semesterId, subjectId);
    }
}
