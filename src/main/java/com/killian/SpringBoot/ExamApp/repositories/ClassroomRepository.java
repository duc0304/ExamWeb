package com.killian.SpringBoot.ExamApp.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.killian.SpringBoot.ExamApp.models.Classroom;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    @Query("SELECT c FROM Classroom c WHERE c.name = :name AND c.teacher = :teacher")
    Classroom findByNameAndTeacher(@Param("name") String name, @Param("teacher") String teacher);

    @Query("SELECT c FROM Classroom c WHERE c.name = :name")
    Classroom findByName(@Param("name") String name);

    @Query("SELECT c.classCode FROM Classroom c WHERE c.name = :name")
    String classCodeByName(@Param("name") String name);

    @Query("SELECT c FROM Classroom c WHERE c.teacher = :teacher")
    List<Classroom> findByTeacher(@Param("teacher") String teacher);

    @Query("SELECT c FROM Classroom c WHERE c.classCode = :classCode")
    Classroom findByClasscode(@Param("classCode") String classCode);
}
