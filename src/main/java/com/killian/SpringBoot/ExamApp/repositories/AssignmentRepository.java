package com.killian.SpringBoot.ExamApp.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.killian.SpringBoot.ExamApp.models.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    @Transactional
    @Modifying
    @Query("DELETE FROM Assignment a WHERE a.assignmentId = :assignmentId")
    void deleteByAssignmentId(@Param("assignmentId") String assignmentId);

    @Query("SELECT a FROM Assignment a WHERE a.className = :className")
    List<Assignment> findAssignmentsByClassname(@Param("className") String className);

    @Query("SELECT a FROM Assignment a WHERE a.classCode = :classCode")
    List<Assignment> findAssignmentsByClasscode(@Param("classCode") String classCode);

    @Query("SELECT a FROM Assignment a WHERE a.assignmentId = :assignmentId")
    Assignment findByAssignmentId(@Param("assignmentId") String assignmentId);

    @Query("SELECT a FROM Assignment a WHERE a.className = :className AND a.name = :name")
    List<Assignment> findAssignmentByName(@Param("className") String className, @Param("name") String name);

    @Query("SELECT a FROM Assignment a WHERE a.classCode = :classCode AND a.name = :name")
    Assignment findAssignmentByClasscodeAndName(@Param("classCode") String classCode, @Param("name") String name);
}
