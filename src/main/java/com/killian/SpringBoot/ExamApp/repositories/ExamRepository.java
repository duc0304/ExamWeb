package com.killian.SpringBoot.ExamApp.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.killian.SpringBoot.ExamApp.models.Exam;

public interface ExamRepository extends JpaRepository<Exam, Long> {

        List<Exam> findByExamId(String examId);

        @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Exam e WHERE e.name = :examName")
        boolean existsByExamName(String examName);

        @Query("SELECT DISTINCT e.subject FROM Exam e")
        List<String> findDistinctSubjects();

        @Query("SELECT DISTINCT e.grade FROM Exam e")
        List<Integer> findDistinctGrades();

        @Query("SELECT e FROM Exam e WHERE e.subject = :subject")
        List<Exam> findBySubject(@Param("subject") String subject);

        @Query("SELECT e FROM Exam e WHERE e.subject = :subject AND e.grade = :grade AND e.examCode = :examCode")
        List<Exam> findBySubjectGradeAndCode(@Param("subject") String subject, @Param("grade") int grade,
                        @Param("examCode") int examCode);

        @Query("SELECT e FROM Exam e WHERE e.subject = :subject AND e.grade = :grade AND e.examCode = :examCode AND e.owner = :owner")
        List<Exam> findPrivateExamBySubjectGradeAndCode(@Param("subject") String subject, @Param("grade") int grade,
                        @Param("examCode") int examCode, @Param("owner") String owner);

        @Query("SELECT e FROM Exam e WHERE e.subject = :subject AND e.grade = :grade AND e.examCode = :examCode AND e.owner IS NULL")
        List<Exam> findTrainingExams(@Param("subject") String subject, @Param("grade") int grade,
                        @Param("examCode") int examCode);

        @Query("SELECT e FROM Exam e WHERE e.name = :name AND e.owner = :owner")
        List<Exam> findByNameAndOwner(@Param("name") String name, @Param("owner") String owner);

        @Query("SELECT e FROM Exam e WHERE e.name = :name")
        List<Exam> findByName(@Param("name") String name);

        @Query("SELECT e FROM Exam e WHERE e.examId = :examId AND e.examCode = :examCode")
        Exam findByExamIdAndCode(@Param("examId") String examId, @Param("examCode") int examCode);

        @Query("SELECT DISTINCT e.examCode FROM Exam e WHERE e.examId = :examId")
        List<Integer> findDistinctExamCode(@Param("examId") String examId);

        @Transactional
        @Modifying
        @Query("DELETE FROM Exam e WHERE e.examId = :examId")
        void deleteByExamId(@Param("examId") String examId);
}
