package com.myfschool.repository;

import com.myfschool.entity.Club;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRepository extends JpaRepository<Club, Long> {

    List<Club> findByStatus(String status);
}
