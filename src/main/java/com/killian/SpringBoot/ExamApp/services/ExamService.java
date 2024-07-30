package com.killian.SpringBoot.ExamApp.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.killian.SpringBoot.ExamApp.models.Exam;
import com.killian.SpringBoot.ExamApp.models.Question;
import com.killian.SpringBoot.ExamApp.repositories.ExamRepository;
import com.killian.SpringBoot.ExamApp.repositories.QuestionRepository;

@Service
@SuppressWarnings("null")
public class ExamService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private FormulaService formulaService;

    public Exam createExam(String examName, List<Long> questionIds) {

        Exam exam = new Exam();
        exam.setName(examName);

        List<Question> selectedQuestions = questionRepository.findAllById(questionIds);

        exam.setQuestions(selectedQuestions);

        return examRepository.save(exam);
    }

    public Exam getExamById(Long examId) {
        return examRepository.findById(examId).orElse(null);
    }

    public Exam processDocxFile(MultipartFile file, int grade, String subject) {

        Exam exam = new Exam();
        XWPFDocument document;
        try {
            document = new XWPFDocument(file.getInputStream());

            List<Question> questions = new ArrayList<>();
            Question currentQuestion = null;
            List<String> currentChoices = null;
            List<String> currentAnswers = null;

            boolean hasParam = false;
            Map<String, Double> paramValue = null;

            for (XWPFParagraph paragraph : document.getParagraphs()) {

                if (paragraph.getRuns().isEmpty())
                    continue;

                String text = "";
                for (XWPFRun run : paragraph.getRuns()) {
                    text = text + run.getText(0);
                }

                if (text == "") {
                    continue;
                }

                XWPFRun run = paragraph.getRuns().get(0);

                if (text.startsWith("Câu ") || text.startsWith("*Câu ") || text.startsWith("#Câu ")) {
                    if (currentQuestion != null) {
                        currentQuestion.setChoices(currentChoices);
                        currentQuestion.setAnswer(currentAnswers);
                        currentQuestion.shuffleChoices(); // shuffle choices
                        questionRepository.save(currentQuestion);
                        questions.add(currentQuestion);
                    }
                    currentQuestion = new Question(grade, subject);
                    currentChoices = new ArrayList<>();
                    currentAnswers = new ArrayList<>();
                    for (int i = 5; i < text.length(); i++) {
                        char c = text.charAt(i);
                        if (c == ':') {
                            currentQuestion.setText(text.substring(i + 2));
                            break;
                        }
                    }
                    if (text.startsWith("C"))
                        currentQuestion.setQuestionType("multiple-choice");
                    else if (text.startsWith("*")) {
                        hasParam = true;
                        paramValue = generateValue(paragraph, currentQuestion);
                    }
                    if (text.startsWith("#"))
                        currentQuestion.setQuestionType("ranking");

                } else if (currentQuestion != null && currentChoices != null && currentAnswers != null) {
                    if (text.startsWith("A. ")
                            || text.startsWith("B. ")
                            || text.startsWith("C. ")
                            || text.startsWith("D. ")
                            || text.startsWith("E. ")
                            || text.startsWith("G. ")
                            || text.startsWith("H. ")) {

                        currentChoices.add(text.substring(3));
                        if (run.isBold())
                            currentAnswers.add(text.substring(3));
                    } else {
                        if (currentQuestion.getQuestionType() == null
                                || currentQuestion.getQuestionType().equals("multiple-choice"))
                            currentQuestion.setQuestionType("type-in");
                        if (run.isBold())
                            currentAnswers.add(text);
                    }
                    if (hasParam && text.startsWith("Công thức") && paramValue != null) {
                        String formula = text.substring(11);
                        for (String key : paramValue.keySet()) {
                            formula = formula.replaceAll(key, String.valueOf(paramValue.get(key)));
                        }
                        currentAnswers.add(formulaService.calculate(formula) + "");
                    }
                    if (text.startsWith("Thứ tự")) {
                        String correctOrder = "";
                        for (int i = 7; i < text.length(); i++) {
                            char id = text.charAt(i);
                            if (id >= 'A' && id <= 'Z') {
                                if (correctOrder.equals("")) {
                                    correctOrder = currentChoices.get(id - 'A');
                                    continue;
                                }
                                correctOrder = correctOrder + " --> " + currentChoices.get(id - 'A');
                            }
                        }
                        currentAnswers.add(correctOrder);
                    }
                }
            }
            // Add the last question to the current exam
            if (currentQuestion != null) {
                currentQuestion.setChoices(currentChoices);
                currentQuestion.setAnswer(currentAnswers);
                currentQuestion.shuffleChoices(); // shuffle choices
                questionRepository.save(currentQuestion);
                questions.add(currentQuestion);
                if (exam != null) {
                    Collections.shuffle(questions); // shuffle questions
                    exam.setQuestions(questions);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return exam;
    }

    public Map<String, Double> generateValue(XWPFParagraph paragraph, Question question) {

        String newQuestionText = question.getText();
        Random random = new Random();
        Map<String, Double> hashMap = new HashMap<>();
        for (int k = 0; k < paragraph.getRuns().size(); k++) {
            XWPFRun run = paragraph.getRuns().get(k);
            if (run.getUnderline() != UnderlinePatterns.NONE) {
                String text = run.getText(0);
                for (int k2 = k + 1; k2 < paragraph.getRuns().size(); k2++) {
                    XWPFRun run2 = paragraph.getRuns().get(k2);
                    if (run2.getUnderline() != UnderlinePatterns.NONE)
                        text = text + run2.getText(0);
                    else
                        break;
                }
                String assignValueStr;
                for (int i = 0; i < text.length(); i++) {
                    if (text.charAt(i) == '=') {
                        String param = text.substring(0, i - 1);
                        if (text.charAt(i + 2) == '&') {
                            int isInt = random.nextInt(2);
                            double value = random.nextInt(200) / 2.0 + isInt * 0.5;
                            hashMap.put(param, value);
                            assignValueStr = param + " = " + String.valueOf(value);
                            newQuestionText = newQuestionText.replaceAll(text, assignValueStr);
                        } else {
                            String valueInStr = "";
                            for (int j = i + 2; j < text.length(); j++) {
                                if ((text.charAt(j) >= '0' && text.charAt(j) <= '9') || text.charAt(j) == '.')
                                    valueInStr = valueInStr + text.charAt(j);
                            }
                            hashMap.put(param, Double.parseDouble(valueInStr));
                        }
                        break;
                    }
                }
            }
        }
        question.setText(newQuestionText);
        return hashMap;
    }

}