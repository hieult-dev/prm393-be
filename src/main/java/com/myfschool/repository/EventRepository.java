package com.myfschool.repository;

import com.myfschool.entity.Event;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatusOrderByStartTimeDesc(String status);
}
