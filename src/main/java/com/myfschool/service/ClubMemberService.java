package com.myfschool.service;

import com.myfschool.entity.ClubMember;
import com.myfschool.repository.ClubMemberRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClubMemberService extends AbstractCrudService<ClubMember> {

    private final ClubMemberRepository repository;

    public ClubMemberService(ClubMemberRepository repository) {
        super(repository, "Club member");
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ClubMember> findByClubId(Long clubId) {
        return repository.findByClubId(clubId);
    }

    @Transactional(readOnly = true)
    public List<ClubMember> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }
}
