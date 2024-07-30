package com.killian.SpringBoot.ExamApp.controllers.restcontrollers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.killian.SpringBoot.ExamApp.models.Assignment;
import com.killian.SpringBoot.ExamApp.models.Choice;
import com.killian.SpringBoot.ExamApp.models.Exam;
import com.killian.SpringBoot.ExamApp.models.Question;
import com.killian.SpringBoot.ExamApp.models.Submission;
import com.killian.SpringBoot.ExamApp.repositories.AssignmentRepository;
import com.killian.SpringBoot.ExamApp.repositories.ChoiceRepository;
import com.killian.SpringBoot.ExamApp.repositories.ExamRepository;
import com.killian.SpringBoot.ExamApp.repositories.SubmissionRepository;

@RestController
@RequestMapping(path = "/api/submission")
public class SubmissionController {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ChoiceRepository choiceRepository;

    @PostMapping("/submit-ranking-question")
    @ResponseStatus(HttpStatus.OK)
    public String submitRankingQuestion(
            @RequestParam("submissionId") String submissionId,
            @RequestParam("questionIndex") int questionIndex,
            @RequestParam("choicesJsonString") String choicesJsonString) {
        Submission submission = submissionRepository.findBySubmissionId(submissionId);
        List<Choice> choices = submission.getChoices();
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> choicesInOrder = mapper.readValue(choicesJsonString,
                    new TypeReference<List<String>>() {
                    });
            Choice newChoice = new Choice(choicesInOrder);
            choices.set(questionIndex, choiceRepository.save(newChoice));
            submission.setChoices(choices);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        submissionRepository.save(submission);
        return "OK";
    }

    @PostMapping("/submit-multiple-choice-question")
    @ResponseStatus(HttpStatus.OK)
    public String submitMultipleChoiceQuestion(
            @RequestParam("submissionId") String submissionId,
            @RequestParam("questionIndex") int questionIndex,
            @RequestParam("choicesJsonString") String choicesJsonString) {
        Submission submission = submissionRepository.findBySubmissionId(submissionId);
        List<Choice> choices = submission.getChoices();
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> choicesInOrder = mapper.readValue(choicesJsonString,
                    new TypeReference<List<String>>() {
                    });
            Choice newChoice = new Choice(choicesInOrder);
            choices.set(questionIndex, choiceRepository.save(newChoice));
            submission.setChoices(choices);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        submissionRepository.save(submission);
        return "OK";
    }

    @PostMapping("/submit-type-in-question")
    @ResponseStatus(HttpStatus.OK)
    public String submitTypeInQuestion(
            @RequestParam("submissionId") String submissionId,
            @RequestParam("questionIndex") int questionIndex,
            @RequestParam("typeInInput") String typeInInput) {
        Submission submission = submissionRepository.findBySubmissionId(submissionId);
        List<Choice> choices = submission.getChoices();
        Choice newChoice = new Choice(Arrays.asList(typeInInput));
        choices.set(questionIndex, choiceRepository.save(newChoice));
        submission.setChoices(choices);
        submissionRepository.save(submission);
        return "OK";
    }

    @PostMapping("submit-assignment")
    @ResponseStatus(HttpStatus.OK)
    public String submitAssignment(
            @RequestParam("submissionId") String submissionId) {

        Submission submission = submissionRepository.findBySubmissionId(submissionId);
        Assignment assignment = assignmentRepository.findByAssignmentId(submission.getAssignmentId());
        Exam exam = examRepository.findByExamIdAndCode(assignment.getExamId(), submission.getExamCode());
        List<Question> questions = exam.getQuestions();
        int countCorrectAnswer = 0;
        List<Choice> choices = submission.getChoices();
        if (choices != null) {
            for (int i = 0; i < questions.size(); i++) {
                Question question = questions.get(i);
                List<String> answer = question.getAnswer();
                Choice curChoice = choices.get(i);
                if (curChoice == null)
                    continue;
                if (question.getQuestionType().equals("type-in")) {
                    List<String> selections = choices.get(i).getSelections();
                    if (selections == null || selections.isEmpty() || selections.get(0) == null)
                        continue;
                    for (String aAnswer : answer) {
                        if (aAnswer.endsWith(".0")) {
                            if (aAnswer.substring(0, aAnswer.length() - 2).equals(selections.get(0))) {
                                countCorrectAnswer++;
                                curChoice.setIsCorrect(1);
                                choiceRepository.save(curChoice);
                                break;
                            }
                        } else if (aAnswer.equals(selections.get(0))) {
                            countCorrectAnswer++;
                            curChoice.setIsCorrect(1);
                            choiceRepository.save(curChoice);
                            break;
                        }
                    }
                } else if (question.getQuestionType().equals("multiple-choice")) {
                    List<String> selections = choices.get(i).getSelections();
                    if (selections == null || selections.isEmpty() || selections.get(0) == null)
                        continue;
                    if (answer.size() != selections.size())
                        continue;
                    Collections.sort(answer);
                    Collections.sort(selections);
                    for (int j = 0; j < answer.size(); j++) {
                        if (!answer.get(j).equals(selections.get(j)))
                            break;
                        if (j == answer.size() - 1) {
                            countCorrectAnswer++;
                            curChoice.setIsCorrect(1);
                            choiceRepository.save(curChoice);
                        }
                    }
                } else if (question.getQuestionType().equals("ranking")) {
                    List<String> selections = choices.get(i).getSelections();
                    if (selections == null || selections.isEmpty() || selections.get(0) == null)
                        continue;
                    String[] array = answer.get(0).split(" --> ");
                    if (array.length != selections.size())
                        continue;
                    for (int j = 0; j < array.length; j++) {
                        if (!array[j].equals(selections.get(j)))
                            break;
                        if (j == array.length - 1) {
                            countCorrectAnswer++;
                            curChoice.setIsCorrect(1);
                            choiceRepository.save(curChoice);
                        }
                    }
                }
            }
        }
        double newScore = (double) countCorrectAnswer / questions.size() * 10.0;
        double roundedScore = Math.round(newScore * 10) / 10.0;
        submission.setScore(roundedScore);
        submission.setSubmittedTime(getCurrentDateTime());
        submissionRepository.save(submission);
        return "OK";
    }

    @PostMapping("submit-free-exam")
    @ResponseStatus(HttpStatus.OK)
    public String submitFreeExam(
            @RequestParam("submissionId") String submissionId,
            @RequestParam("examId") String examId) {

        Submission submission = submissionRepository.findBySubmissionId(submissionId);
        Exam exam = examRepository.findByExamIdAndCode(examId, submission.getExamCode());
        List<Question> questions = exam.getQuestions();
        int countCorrectAnswer = 0;
        List<Choice> choices = submission.getChoices();
        if (choices != null) {
            for (int i = 0; i < questions.size(); i++) {
                Question question = questions.get(i);
                List<String> answer = question.getAnswer();
                Choice curChoice = choices.get(i);
                if (curChoice == null)
                    continue;
                if (question.getQuestionType().equals("type-in")) {
                    List<String> selections = choices.get(i).getSelections();
                    if (selections == null || selections.isEmpty() || selections.get(0) == null)
                        continue;
                    for (String aAnswer : answer) {
                        if (aAnswer.endsWith(".0")) {
                            if (aAnswer.substring(0, aAnswer.length() - 2).equals(selections.get(0))) {
                                countCorrectAnswer++;
                                curChoice.setIsCorrect(1);
                                choiceRepository.save(curChoice);
                                break;
                            }
                        } else if (aAnswer.equals(selections.get(0))) {
                            countCorrectAnswer++;
                            curChoice.setIsCorrect(1);
                            choiceRepository.save(curChoice);
                            break;
                        }
                    }
                } else if (question.getQuestionType().equals("multiple-choice")) {
                    List<String> selections = choices.get(i).getSelections();
                    if (selections == null || selections.isEmpty() || selections.get(0) == null)
                        continue;
                    if (answer.size() != selections.size())
                        continue;
                    Collections.sort(answer);
                    Collections.sort(selections);
                    for (int j = 0; j < answer.size(); j++) {
                        if (!answer.get(j).equals(selections.get(j)))
                            break;
                        if (j == answer.size() - 1) {
                            countCorrectAnswer++;
                            curChoice.setIsCorrect(1);
                            choiceRepository.save(curChoice);
                        }
                    }
                } else if (question.getQuestionType().equals("ranking")) {
                    List<String> selections = choices.get(i).getSelections();
                    if (selections == null || selections.isEmpty() || selections.get(0) == null)
                        continue;
                    String[] array = answer.get(0).split(" --> ");
                    if (array.length != selections.size())
                        continue;
                    for (int j = 0; j < array.length; j++) {
                        if (!array[j].equals(selections.get(j)))
                            break;
                        if (j == array.length - 1) {
                            countCorrectAnswer++;
                            curChoice.setIsCorrect(1);
                            choiceRepository.save(curChoice);
                        }
                    }
                }
            }
        }
        double newScore = (double) countCorrectAnswer / questions.size() * 10.0;
        double roundedScore = Math.round(newScore * 10) / 10.0;
        submission.setScore(roundedScore);
        submission.setSubmittedTime(getCurrentDateTime());
        submissionRepository.save(submission);
        return "OK";
    }

    private static String getCurrentDateTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss MM/dd/yyyy");
        String formattedDateTime = currentDateTime.format(formatter);
        return formattedDateTime;
    }
}
