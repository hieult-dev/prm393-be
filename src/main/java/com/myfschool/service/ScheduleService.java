package com.myfschool.service;

import com.myfschool.entity.Schedule;
import com.myfschool.repository.ScheduleRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleService extends AbstractCrudService<Schedule> {

    private final ScheduleRepository repository;

    public ScheduleService(ScheduleRepository repository) {
        super(repository, "Schedule");
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Schedule> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Schedule> findByUserIdAndStudyDate(Long userId, LocalDate studyDate) {
        return repository.findByUserIdAndStudyDate(userId, studyDate);
    }
}
