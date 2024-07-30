package com.killian.SpringBoot.ExamApp.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.killian.SpringBoot.ExamApp.models.Question;
import com.killian.SpringBoot.ExamApp.repositories.QuestionRepository;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    public void processDocxFile(MultipartFile file, int grade, String subject) {

        XWPFDocument document;
        try {
            document = new XWPFDocument(file.getInputStream());

            Question currentQuestion = null;
            List<String> currentChoices = null;
            List<String> currentAnswers = null;

            for (XWPFParagraph paragraph : document.getParagraphs()) {

                if (paragraph.getRuns().isEmpty())
                    break;
                XWPFRun run = paragraph.getRuns().get(0);
                String text = run.getText(0);

                if (text == null) {
                    continue;
                }

                if (text.startsWith("Câu ")) {
                    if (currentQuestion != null) {
                        currentQuestion.setChoices(currentChoices);
                        currentQuestion.setAnswer(currentAnswers);
                        questionRepository.save(currentQuestion);
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
                } else if (currentQuestion != null && currentChoices != null && currentAnswers != null) {
                    if (text.startsWith("A. ")
                            || text.startsWith("B. ")
                            || text.startsWith("C. ")
                            || text.startsWith("D. ")) {

                        currentChoices.add(text.substring(3));

                        if (run.isBold())
                            currentAnswers.add(text.substring(3));
                    } else if (text.startsWith("Chương"))
                        currentQuestion.setChapter(text);
                }
            }
            // Add the last question to the current exam
            if (currentQuestion != null) {
                currentQuestion.setChoices(currentChoices);
                questionRepository.save(currentQuestion);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
