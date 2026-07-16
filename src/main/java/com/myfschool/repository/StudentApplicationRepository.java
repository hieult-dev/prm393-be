package com.myfschool.repository;

import com.myfschool.entity.StudentApplication;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentApplicationRepository extends JpaRepository<StudentApplication, Long> {

    List<StudentApplication> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<StudentApplication> findByStatusOrderByCreatedAtDesc(String status);

    @Query("""
            SELECT application
            FROM StudentApplication application
            WHERE application.parentId = :parentId
              AND (:status IS NULL OR UPPER(application.status) = :status)
            ORDER BY application.createdAt DESC, application.id DESC
            """)
    List<StudentApplication> findForParent(
            @Param("parentId") Long parentId,
            @Param("status") String status
    );

    @Query("""
            SELECT application
            FROM StudentApplication application
            WHERE application.parentId = :parentId
              AND application.userId = :studentId
              AND (:status IS NULL OR UPPER(application.status) = :status)
            ORDER BY application.createdAt DESC, application.id DESC
            """)
    List<StudentApplication> findForParentStudent(
            @Param("parentId") Long parentId,
            @Param("studentId") Long studentId,
            @Param("status") String status
    );

    @Query("""
            SELECT application
            FROM StudentApplication application
            JOIN User student ON student.id = application.userId
            WHERE LOWER(student.className) = LOWER(:className)
              AND (:status IS NULL OR UPPER(application.status) = :status)
            ORDER BY application.createdAt DESC, application.id DESC
            """)
    List<StudentApplication> findForHomeroomClass(
            @Param("className") String className,
            @Param("status") String status
    );
}