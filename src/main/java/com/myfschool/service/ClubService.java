package com.myfschool.service;

import com.myfschool.entity.Club;
import com.myfschool.repository.ClubRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClubService extends AbstractCrudService<Club> {

    private final ClubRepository repository;

    public ClubService(ClubRepository repository) {
        super(repository, "Club");
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Club> findByStatus(String status) {
        return repository.findByStatus(status);
    }
}
