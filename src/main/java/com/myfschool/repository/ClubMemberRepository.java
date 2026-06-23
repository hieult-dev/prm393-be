package com.myfschool.repository;

import com.myfschool.entity.ClubMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {

    List<ClubMember> findByClubId(Long clubId);

    List<ClubMember> findByUserId(Long userId);
}
