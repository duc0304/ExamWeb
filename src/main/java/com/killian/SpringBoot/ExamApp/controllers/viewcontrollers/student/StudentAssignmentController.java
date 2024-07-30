package com.killian.SpringBoot.ExamApp.controllers.viewcontrollers.student;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.killian.SpringBoot.ExamApp.models.Assignment;
import com.killian.SpringBoot.ExamApp.models.Choice;
import com.killian.SpringBoot.ExamApp.models.Classroom;
import com.killian.SpringBoot.ExamApp.models.Exam;
import com.killian.SpringBoot.ExamApp.models.Question;
import com.killian.SpringBoot.ExamApp.models.Submission;
import com.killian.SpringBoot.ExamApp.repositories.AssignmentRepository;
import com.killian.SpringBoot.ExamApp.repositories.ChoiceRepository;
import com.killian.SpringBoot.ExamApp.repositories.ClassroomRepository;
import com.killian.SpringBoot.ExamApp.repositories.ExamRepository;
import com.killian.SpringBoot.ExamApp.repositories.SubmissionRepository;
import com.killian.SpringBoot.ExamApp.services.SessionManagementService;

@Controller
@RequestMapping(path = "/student/classroom/assignment")
public class StudentAssignmentController {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ChoiceRepository choiceRepository;

    @Autowired
    private SessionManagementService sessionManagementService;

    @GetMapping("")
    public String assignmentList(
            @RequestParam("classCode") String classCode,
            Model model) {
        Classroom classroom = classroomRepository.findByClasscode(classCode);
        List<Assignment> assignments = assignmentRepository.findAssignmentsByClassname(classroom.getName());
        model.addAttribute("assignments", assignments);
        model.addAttribute("className", classroom.getName());
        model.addAttribute("classCode", classCode);
        model.addAttribute("message", sessionManagementService.getMessage());
        sessionManagementService.clearMessage();
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "student/assignments";
    }

    @GetMapping("view-assignment")
    public String viewAssignment(
            @RequestParam("assignmentId") String assignmentId,
            @RequestParam("classCode") String classCode,
            Model model) {
        String className = classroomRepository.findByClasscode(classCode).getName();
        Assignment assignment = assignmentRepository.findByAssignmentId(assignmentId);
        model.addAttribute("assignment", assignment);
        model.addAttribute("assignmentDeadline", assignment.getDeadline());

        Exam exam = examRepository.findByExamId(assignment.getExamId()).get(0);
        model.addAttribute("className", className);
        model.addAttribute("classCode", classCode);
        model.addAttribute("examDuration", exam.getDuration());
        model.addAttribute("message", sessionManagementService.getMessage());
        sessionManagementService.clearMessage();

        Submission submission = submissionRepository.findByAssignmentId(assignment.getAssignmentId(),
                sessionManagementService.getUsername());
        if (submission != null) {
            if (submission.getScore() != -1.0)
                model.addAttribute("submitted", 1);
            else
                model.addAttribute("submitted", -1);
            model.addAttribute("submissionId", submission.getSubmissionId());
        } else {
            model.addAttribute("submitted", 0);
            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
            // 00:49 24/11/2023
            LocalDateTime deadline = LocalDateTime.parse(assignment.getDeadline(), formatter);
            int comparison = deadline.compareTo(currentDateTime);
            if (comparison < 0)
                model.addAttribute("expired", 1);
            else
                model.addAttribute("expired", 0);
        }
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "student/view-assignment";
    }

    @GetMapping("do-assignment")
    public String doAssignment(
            @RequestParam("assignmentId") String assignmentId,
            @RequestParam("classCode") String classCode,
            Model model) {

        Classroom classroom = classroomRepository.findByClasscode(classCode);
        Assignment assignment = assignmentRepository.findByAssignmentId(assignmentId);
        List<Exam> exams = examRepository.findByExamId(assignment.getExamId());
        String student = sessionManagementService.getUsername();

        Submission submission = submissionRepository.findByAssignmentId(assignment.getAssignmentId(), student);
        if (submission == null) {
            model.addAttribute("message", "Bắt đầu làm bài");
            // Get a random examCode
            Random random = new Random();
            int randomIndex = random.nextInt(exams.size());
            Exam exam = exams.get(randomIndex);
            Submission newSubmission = new Submission(student, assignment.getAssignmentId(), randomIndex,
                    exam.getQuestions().size(), exam.getDuration());
            List<Choice> newChoices = new ArrayList<>();
            for (int j = 0; j < exam.getQuestions().size(); j++) {
                newChoices.add(choiceRepository.save(new Choice()));
            }
            newSubmission.setChoices(newChoices);
            submissionRepository.save(newSubmission);
            model.addAttribute("endTime", newSubmission.getEndTime());
            model.addAttribute("choices", newSubmission.getChoices());
            model.addAttribute("submissionId", newSubmission.getSubmissionId());
            model.addAttribute("exam", exam);
        } else {
            Exam exam = exams.get(submission.getExamCode());
            model.addAttribute("endTime", submission.getEndTime());
            model.addAttribute("exam", exam);
            model.addAttribute("submissionId", submission.getSubmissionId());
            model.addAttribute("choices", submission.getChoices());
        }
        model.addAttribute("className", classroom.getName());
        model.addAttribute("classCode", classCode);
        model.addAttribute("assignmentName", assignment.getName());
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "student/do-assignment";
    }

    @GetMapping("result")
    public String getResult(
            @RequestParam("submissionId") String submissionId,
            Model model) {
        Submission submission = submissionRepository.findBySubmissionId(submissionId);
        Assignment assignment = assignmentRepository.findByAssignmentId(submission.getAssignmentId());
        Exam exam = examRepository.findByExamId(assignment.getExamId()).get(submission.getExamCode());
        List<Question> questions = exam.getQuestions();
        List<Choice> selectedChoices = submission.getChoices();
        List<String> choices = new ArrayList<>();
        List<Integer> isCorrect = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            if (selectedChoices.size() < (i + 1) || selectedChoices.get(i) == null
                    || selectedChoices.get(i).getSelections() == null
                    || selectedChoices.get(i).getSelections().size() == 0) {
                choices.add("Không trả lời");
                isCorrect.add(0);
            } else {
                Choice choice = selectedChoices.get(i);
                List<String> selections = choice.getSelections();
                Question question = questions.get(i);
                if (question.getQuestionType().equals("ranking")) {
                    String tmp = "";
                    for (int j = 0; j < selections.size(); j++) {
                        tmp = tmp + selections.get(j);
                        if (j < selections.size() - 1)
                            tmp = tmp + " --> ";
                    }
                    choices.add(tmp);
                }
                if (question.getQuestionType().equals("multiple-choice")) {
                    String tmp = "";
                    for (int j = 0; j < selections.size(); j++) {
                        tmp = tmp + selections.get(j);
                        if (j < selections.size() - 1)
                            tmp = tmp + ", ";
                    }
                    choices.add(tmp);
                }
                if (question.getQuestionType().equals("type-in")) {
                    String tmp = selections.get(0);
                    choices.add(tmp);
                }
                isCorrect.add(choice.getIsCorrect());
            }
        }
        // Thời gian bắt đầu: 17:30:38 11/02/2023
        DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss MM/dd/yyyy");
        DateTimeFormatter desiredformat = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        LocalDateTime startedTime = LocalDateTime.parse(submission.getStartedTime(), format);
        LocalDateTime submittedTime = LocalDateTime.parse(submission.getSubmittedTime(), format);
        model.addAttribute("startedTime", desiredformat.format(startedTime));
        model.addAttribute("submittedTime", desiredformat.format(submittedTime));
        model.addAttribute("choices", choices);
        model.addAttribute("isCorrect", isCorrect);
        model.addAttribute("questions", questions);
        model.addAttribute("submission", submission);
        model.addAttribute("assignment", assignment);
        model.addAttribute("classCode", assignment.getClassCode());
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "student/result";
    }
}
