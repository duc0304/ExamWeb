package com.killian.SpringBoot.ExamApp.controllers.viewcontrollers.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.killian.SpringBoot.ExamApp.models.Question;
import com.killian.SpringBoot.ExamApp.repositories.QuestionRepository;
import com.killian.SpringBoot.ExamApp.services.QuestionService;
import com.killian.SpringBoot.ExamApp.services.SessionManagementService;

@Controller
@RequestMapping(path = "/admin/questions")
public class AdminQuestionController {

    @Autowired
    private SessionManagementService sessionManagementService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionService questionService;

    @GetMapping("/create-question-page")
    public String createQuestionPage(Model model) {
        String username = sessionManagementService.getUsername();
        model.addAttribute("username", username);
        model.addAttribute("message", sessionManagementService.getMessage());
        sessionManagementService.clearMessage();
        return "admin/create-question";
    }

    @GetMapping("/create-single-question-page")
    public String createSingleQuestionPage(Model model) {
        model.addAttribute("message", sessionManagementService.getMessage());
        sessionManagementService.clearMessage();
        return "admin/create-single-question";
    }

    @PostMapping("/create-question")
    public String createQuestion(
            @RequestParam("text") String text,
            @RequestParam(value = "choices", required = false) List<String> choices,
            @RequestParam("answer") List<String> answer,
            @RequestParam("subject") String subject,
            @RequestParam("grade") int grade,
            @RequestParam(value = "chapter", required = false) String chapter,
            @RequestParam("questionType") String questionType,
            Model model) {

        Question newQuestion = new Question();
        newQuestion.setText(text);
        if (choices != null)
            newQuestion.setChoices(choices);
        newQuestion.setAnswer(answer);
        newQuestion.setSubject(subject);
        newQuestion.setGrade(grade);
        if (chapter != null)
            newQuestion.setChapter(chapter);
        newQuestion.setQuestionType(questionType);

        if (questionType.equals("multiple-choice") && (choices == null || choices.size() < 2))
            sessionManagementService.setMessage("Câu hỏi trắc nghiệm phải có số đáp án >= 2");
        else
            try {
                questionRepository.save(newQuestion);
                sessionManagementService.setMessage("Thành công! Câu hỏi đã được thêm vào database.");
            } catch (Exception e) {
                sessionManagementService.setMessage("Thất bại! Trùng lặp câu hỏi.");
            }
        return "redirect:/admin/questions/create-question-page";
    }

    @PostMapping("/create-multiple-questions")
    public String createMultipleQuestions(
            @RequestParam("subject") String subject,
            @RequestParam("grade") int grade,
            @RequestParam("file") MultipartFile file,
            Model model) {

        questionService.processDocxFile(file, grade, subject);
        sessionManagementService.setMessage("Thêm câu hỏi thành công!");
        return "redirect:/admin/questions/create-multiple-questions-page";
    }

    @GetMapping("/create-multiple-questions-page")
    public String createMultipleQuestionsPage(Model model) {
        model.addAttribute("message", sessionManagementService.getMessage());
        sessionManagementService.clearMessage();
        return "admin/create-questions-by-docx";
    }

    @GetMapping("/view-questions-by-filter-page")
    public String getQuestionsByFilterPage(Model model) {

        List<String> subjects = questionRepository.findDistinctSubjects();
        List<String> difficulties = questionRepository.findDistinctDifficuties();

        model.addAttribute("subjects", subjects);
        model.addAttribute("difficulties", difficulties);

        // Initially, display questions from the first subject
        if (!subjects.isEmpty()) {
            List<Question> questions = questionRepository.findBySubjectAndDifficulty(subjects.get(0),
                    difficulties.get(0));
            model.addAttribute("selectedSubject", subjects.get(0));
            model.addAttribute("selectedDifficulty", difficulties.get(0));
            model.addAttribute("questions", questions);
        }

        return "admin/questions-by-filter";
    }
}
