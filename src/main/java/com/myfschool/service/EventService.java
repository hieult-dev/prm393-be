package com.myfschool.service;

import com.myfschool.entity.Event;
import com.myfschool.repository.EventRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService extends AbstractCrudService<Event> {

    private final EventRepository repository;

    public EventService(EventRepository repository) {
        super(repository, "Event");
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Event> findByStatus(String status) {
        return repository.findByStatus(status);
    }
}
