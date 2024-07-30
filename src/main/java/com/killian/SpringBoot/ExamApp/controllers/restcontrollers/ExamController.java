package com.killian.SpringBoot.ExamApp.controllers.restcontrollers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.killian.SpringBoot.ExamApp.models.Exam;
import com.killian.SpringBoot.ExamApp.models.ResponseObject;
import com.killian.SpringBoot.ExamApp.repositories.ExamRepository;
import com.killian.SpringBoot.ExamApp.services.ExamService;

@RestController
@SuppressWarnings("null")
@RequestMapping(path = "/api/exam")
// Request: http://localhost:8080/api/exam
public class ExamController {

    // Dependency Injection
    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamService examService;

    // Get all products
    @GetMapping("/getAllExams")
    List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    // Get exam by id
    @GetMapping("/getExam/{id}")
    ResponseEntity<ResponseObject> findById(@PathVariable Long id) {
        Optional<Exam> foundExam = examRepository.findById(id);
        if (foundExam.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseObject("Ok", "Get exam successfully", foundExam));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject("Fail", "Can not find exam with id = " + id, null));
        }
    }

    // Delete exam by id
    @DeleteMapping("/deleteExam/{id}")
    ResponseEntity<ResponseObject> deleteById(@PathVariable Long id) {
        boolean examExist = examRepository.existsById(id);
        if (examExist) {
            examRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseObject("Ok", "Delete exam successfully", null));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject("Fail", "Can not find exam with id = " + id, null));
        }
    }

    // Create new exam
    @PostMapping("/createExam")
    public ResponseEntity<ResponseObject> createExam(@RequestParam String examName,
            @RequestBody List<Long> questionIds) {
        if (!examRepository.existsByExamName(examName)) {
            Exam exam = examService.createExam(examName, questionIds);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseObject("Ok", "Exam created successfully", exam));
        } else
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(new ResponseObject("Ok", "Duplicated exam name", null));
    }

    // view an exam
    @GetMapping("/viewExam/{id}")
    public ResponseEntity<ResponseObject> viewExam(@PathVariable Long id) {
        Optional<Exam> optionalExam = examRepository.findById(id);
        Exam anExam = new Exam();
        if (optionalExam.isPresent()) {
            anExam = optionalExam.get();
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseObject("OK", "Get exam success", anExam));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject("Fail", "Exam not found", null));
        }
    }

    @GetMapping("/check-exam")
    public String checkExam(@RequestParam String examId) {
        List<Exam> exams = examRepository.findByExamId(examId);
        if (!exams.isEmpty()) {
            return "Tồn tại bài kiểm tra.";
        } else {
            return "Không tồn tại bài kiểm tra trong hệ thống. Hãy kiểm tra lại ID.";
        }
    }
}
