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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.killian.SpringBoot.ExamApp.models.Question;
import com.killian.SpringBoot.ExamApp.models.ResponseObject;
import com.killian.SpringBoot.ExamApp.repositories.QuestionRepository;

@RestController
@SuppressWarnings("null")
@RequestMapping(path = "/api/v1/questions")
// Request: http://localhost:8080/api/v1/questions
public class QuestionController {

    // Dependency Injection
    @Autowired
    private QuestionRepository questionRepository;

    // Get all products
    @GetMapping("/getAllQuestions")
    List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    // Get Question by id
    @GetMapping("/getQuestion/{id}")
    ResponseEntity<ResponseObject> findById(@PathVariable Long id) {
        Optional<Question> foundQuestion = questionRepository.findById(id);
        if (foundQuestion.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseObject("Ok", "Get question successfully", foundQuestion));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject("Fail", "Can not find question with id = " + id, null));
        }
    }

    // Delete question by id
    @DeleteMapping("/deleteQuestion/{id}")
    ResponseEntity<ResponseObject> deleteById(@PathVariable Long id) {
        boolean questionExist = questionRepository.existsById(id);
        if (questionExist) {
            questionRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseObject("Ok", "Delete question successfully", null));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject("Fail", "Can not find question with id = " + id, null));
        }
    }

    @DeleteMapping("/deleteAllQuestions")
    ResponseEntity<ResponseObject> deleteAll() {
        questionRepository.deleteAll();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseObject("Ok", "Delete question successfully", null));
    }

    // Insert new question
    @PostMapping("/insertQuestion")
    ResponseEntity<ResponseObject> insertQuestion(@RequestBody Question newQuestion) {
        List<Question> foundQuestions = questionRepository.findByText(newQuestion.getText().trim());
        if (foundQuestions.size() > 0)
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(new ResponseObject("Not implemented", "Question already in database",
                            null));
        else {
            questionRepository.save(newQuestion);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseObject("Ok", "Insert question successfully", newQuestion));
        }
    }

    // Update question by id
    @PutMapping("/updateQuestion/{id}")
    ResponseEntity<ResponseObject> updateById(@PathVariable Long id, @RequestBody Question newQuestion) {
        Question updatedQuestion = questionRepository.findById(id)
                .map(question -> {
                    question.setText(newQuestion.getText());
                    question.setChoices(newQuestion.getChoices());
                    question.setAnswer(newQuestion.getAnswer());
                    return questionRepository.save(question); // Return the updated question
                })
                .orElseGet(() -> {
                    return questionRepository.save(newQuestion);
                });
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("Ok", "Question updated", updatedQuestion));
    }

    @GetMapping("/getChapters")
    public List<String> getChapters(
            @RequestParam(value = "subject") String subject,
            @RequestParam(value = "grade", required = false) int grade) {
        if (subject == null)
            return null;
        return questionRepository.findDistinctChaptersBySubjectAndGrade(subject, grade);
    }

}
