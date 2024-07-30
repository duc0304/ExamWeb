package com.killian.SpringBoot.ExamApp.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.killian.SpringBoot.ExamApp.models.StudentClassroom;

public interface StudentClassroomRepository extends JpaRepository<StudentClassroom, Long> {
    @Query("SELECT DISTINCT sc.student FROM StudentClassroom sc WHERE sc.classCode = :classCode")
    List<String> findAllStudentsByClasscode(@Param("classCode") String classCode);

    @Query("SELECT sc FROM StudentClassroom sc WHERE sc.classCode = :classCode AND sc.student = :student")
    StudentClassroom findRecord(@Param("student") String student, @Param("classCode") String classCode);

    @Query("SELECT sc FROM StudentClassroom sc WHERE sc.classCode = :classCode AND sc.student = :student")
    StudentClassroom findRecordByClasscode(@Param("student") String student, @Param("classCode") String classCode);

    @Query("SELECT sc FROM StudentClassroom sc WHERE sc.classCode = :classCode")
    List<StudentClassroom> findAllRecordByClasscode(@Param("classCode") String classCode);

    @Query("SELECT sc FROM StudentClassroom sc WHERE sc.student = :student")
    List<StudentClassroom> findAllClassByStudent(@Param("student") String student);
}
