package com.killian.SpringBoot.ExamApp.controllers.viewcontrollers.teacher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.text.Normalizer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.killian.SpringBoot.ExamApp.models.Exam;
import com.killian.SpringBoot.ExamApp.models.Question;
import com.killian.SpringBoot.ExamApp.repositories.ExamRepository;
import com.killian.SpringBoot.ExamApp.repositories.QuestionRepository;
import com.killian.SpringBoot.ExamApp.services.ExamService;
import com.killian.SpringBoot.ExamApp.services.LabelGenerator;
import com.killian.SpringBoot.ExamApp.services.SessionManagementService;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(path = "/teacher/exam")
public class TeacherExamController {

    @Autowired
    private SessionManagementService sessionManagementService;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ExamService examService;

    @Autowired
    private LabelGenerator labelGenerator;

    @GetMapping("/create-exam-page")
    public String createExamPage(Model model) {
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "teacher/create-exam";
    }

    @GetMapping("/select-subject-and-grade")
    public String selectSubjectAndGrade(Model model) {

        List<String> subjects = questionRepository.findDistinctSubjects();
        List<Integer> grades = questionRepository.findDistinctGrades();
        model.addAttribute("subjects", subjects);
        model.addAttribute("grades", grades);
        model.addAttribute("message", sessionManagementService.getMessage());
        sessionManagementService.clearMessage();
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "teacher/select-subject-and-grade";
    }

    @GetMapping("/create-exam-from-bank-page")
    public String createExamFromBankPage(
            @RequestParam("selectedSubject") String selectedSubject,
            @RequestParam("selectedGrade") int selectedGrade,
            Model model) {

        List<String> chapters = questionRepository.findDistinctChaptersBySubjectAndGrade(selectedSubject,
                selectedGrade);

        model.addAttribute("selectedGrade", selectedGrade);
        model.addAttribute("selectedSubject", selectedSubject);
        model.addAttribute("chapters", chapters);

        List<Integer> limit = new ArrayList<>();
        for (String chapter : chapters) {
            limit.add(questionRepository.findNumberOfDistinctQuestionsByChapterAndGrade(chapter, selectedGrade));
        }
        model.addAttribute("limit", limit);
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "teacher/create-exam-from-bank";
    }

    @PostMapping("/create-exam-from-bank")
    public String createExams(
            @RequestParam("subject") String subject,
            @RequestParam("grade") int grade,
            @RequestParam("name") String name,
            @RequestParam("amount") int amount,
            @RequestParam("duration") int duration,
            @RequestParam("questionCountForEachChapter") List<Integer> questionCountForEachChapter,
            Model model) {

        if (!examRepository.findByNameAndOwner(name, sessionManagementService.getUsername()).isEmpty()) {
            sessionManagementService.setMessage("Tên đề thi không hợp lệ!");
            return "redirect:/teacher/exam/select-subject-and-grade";
        }

        String tmp = null;
        for (int j = 0; j < amount; j++) {

            Exam newExam = new Exam();
            newExam.setName(name);
            newExam.setSubject(subject);
            newExam.setGrade(grade);
            newExam.setExamCode(j);
            newExam.setDuration(duration);
            newExam.setOwner(sessionManagementService.getUsername());
            if (tmp != null)
                newExam.setExamId(tmp);
            else {
                newExam.setExamId();
                tmp = newExam.getExamId();
            }
            List<Question> questions = new ArrayList<>();
            List<String> chapters = questionRepository.findDistinctChaptersBySubjectAndGrade(subject, grade);
            for (int i = 0; i < chapters.size(); i++) {
                int count = questionCountForEachChapter.get(i);
                if (count == 0)
                    continue;
                String chapter = chapters.get(i);
                List<String> newQuestionTexts = questionRepository.findDistinctRandomQuestionTextsByChapterGradeSubject(
                        chapter, subject, grade, count);
                for (String text : newQuestionTexts) {
                    Question newQuestion = questionRepository.findFirstQuestionByText(text);
                    questions.add(newQuestion);
                }
            }
            if (j > 0) {
                Collections.shuffle(questions); // shuffle questions
                for (int i = 0; i < questions.size(); i++) { // shuffle choices of each question
                    Question oldQuestion = questions.get(i);
                    Question newQuestion = new Question(oldQuestion.getText(), oldQuestion.getChoices(),
                            oldQuestion.getAnswer(), oldQuestion.getSubject(), oldQuestion.getChapter(),
                            oldQuestion.getGrade(), oldQuestion.getQuestionType());
                    newQuestion.shuffleChoices();
                    newQuestion.resetAnswers();
                    questions.set(i, questionRepository.save(newQuestion));
                }
            }
            newExam.setQuestions(questions);
            examRepository.save(newExam);
        }
        sessionManagementService.setMessage("Tạo đề thi thành công!");
        return "redirect:/teacher/exam/get-exam-by-examId?examId=" + tmp + "&selectedCode=0";
    }

    @GetMapping("/create-own-exam-page")
    public String createExamManuallyPage(Model model) {
        model.addAttribute("message", sessionManagementService.getMessage());
        sessionManagementService.clearMessage();
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "teacher/create-exam-by-docx";
    }

    @PostMapping("/upload-docx")
    public String createExamFromDocx(@RequestParam("subject") String subject,
            @RequestParam("grade") int grade,
            @RequestParam("name") String name,
            @RequestParam("duration") int duration,
            @RequestParam("amount") int amount,
            @RequestParam("file") MultipartFile file,
            Model model) {

        List<Exam> exams = examRepository.findByNameAndOwner(name, sessionManagementService.getUsername());
        if (!exams.isEmpty()) {
            sessionManagementService.setMessage("Tên đề thi trùng lặp");
            return "redirect:/teacher/exam/create-own-exam-page";
        }

        String tmp = null;
        for (int i = 0; i < amount; i++) {
            // process docx file
            Exam exam = examService.processDocxFile(file, grade, subject);
            exam.setName(name);
            exam.setGrade(grade);
            exam.setSubject(subject);
            exam.setDuration(duration);
            exam.setExamCode(i);
            if (i == 0) {
                exam.setExamId();
                tmp = exam.getExamId();
            } else
                exam.setExamId(tmp);
            exam.setOwner(sessionManagementService.getUsername());
            examRepository.save(exam);
        }

        sessionManagementService.setMessage("Tạo đề thi thành công!");
        return "redirect:/teacher/exam/get-exam-by-examId?examId=" + tmp + "&selectedCode=0";
    }

    @GetMapping("/view-exams-by-filter-page")
    public String getQuestionsByFilterPage(Model model) {

        List<String> subjects = examRepository.findDistinctSubjects();
        List<Integer> grades = examRepository.findDistinctGrades();

        model.addAttribute("subjects", subjects);
        model.addAttribute("grades", grades);

        // Initially, display questions from the first subject
        if (!subjects.isEmpty())
            model.addAttribute("selectedSubject", subjects.get(0));
        if (!grades.isEmpty())
            model.addAttribute("selectedGrade", grades.get(0));

        model.addAttribute("message", sessionManagementService.getMessage());
        sessionManagementService.clearMessage();
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "teacher/exams-by-filter";
    }

    @GetMapping("/get-exams-by-subject-and-grade")
    public String getExamsBySelectedSubject(
            @RequestParam("selectedSubject") String selectedSubject,
            @RequestParam("selectedGrade") int selectedGrade,
            Model model) {

        List<String> subjects = examRepository.findDistinctSubjects();
        List<Integer> grades = examRepository.findDistinctGrades();

        model.addAttribute("subjects", subjects);
        model.addAttribute("grades", grades);

        List<Exam> exams = examRepository.findPrivateExamBySubjectGradeAndCode(selectedSubject, selectedGrade, 0,
                sessionManagementService.getUsername());

        model.addAttribute("selectedSubject", selectedSubject);
        model.addAttribute("selectedGrade", selectedGrade);
        model.addAttribute("exams", exams);

        model.addAttribute("message", sessionManagementService.getMessage());
        sessionManagementService.clearMessage();
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "teacher/exams-by-filter";
    }

    @GetMapping("/get-exam-by-examId")
    public String viewExam(
            @RequestParam("examId") String examId,
            @RequestParam("selectedCode") int selectedCode,
            Model model) {

        List<Exam> exams = examRepository.findByExamId(examId);
        Exam exam = exams.get(selectedCode);
        List<Integer> examCodes = examRepository.findDistinctExamCode(examId);
        model.addAttribute("exam", exam);
        model.addAttribute("examCodes", examCodes);
        model.addAttribute("selectedCode", selectedCode);
        model.addAttribute("message", sessionManagementService.getMessage());
        sessionManagementService.clearMessage();
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "teacher/exam-by-examId";
    }

    @GetMapping("/remove-exam")
    public String removeExam(
            @RequestParam("examId") String examId,
            @RequestParam("selectedSubject") String selectedSubject,
            @RequestParam("selectedGrade") int selectedGrade, Model model) {
        try {
            Exam exam = examRepository.findByExamIdAndCode(examId, 0);
            sessionManagementService.setMessage("Bạn đã xóa đề " + exam.getName());
            examRepository.deleteByExamId(examId);
            return "redirect:/teacher/exam/get-exams-by-subject-and-grade?selectedSubject="
                    + URLEncoder.encode(selectedSubject, "UTF-8")
                    + "&selectedGrade=" + selectedGrade;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "redirect:/teacher/exam/view-exams-by-filter-page";
        }
    }

    @GetMapping("/export-pdf")
    public void exportExamsZip(
            HttpServletResponse response,
            @RequestParam("examId") String examId) {
        response.setContentType("application/zip");
        String fileName = convertVietnameseToLatin(examRepository.findByExamIdAndCode(examId, 0).getName());
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".zip");

        try {
            // Create a temporary directory to store individual PDFs
            File tempDir = Files.createTempDirectory("exams").toFile();

            // Create a ZIP file to store the individual PDFs
            File zipFile = new File(tempDir, "exams.zip");
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));

            List<Integer> examCodes = examRepository.findDistinctExamCode(examId);

            for (int k = 0; k < examCodes.size(); k++) {

                int examCode = examCodes.get(k);
                // Generate individual PDFs for each exam
                Exam exam = examRepository.findByExamIdAndCode(examId, examCode);

                PDDocument document = new PDDocument();
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                // Create a content stream for the page
                PDPageContentStream contentStream = new PDPageContentStream(document, page);

                ClassPathResource fontResource = new ClassPathResource("/static/fonts/Cambria Math.ttf");
                ClassPathResource BoldFontResource = new ClassPathResource("/static/fonts/Cambria Bold.ttf");
                PDType0Font font = PDType0Font.load(document, fontResource.getFile());
                PDType0Font boldFont = PDType0Font.load(document, BoldFontResource.getFile());

                // Exam details
                contentStream.setFont(boldFont, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Bài thi: " + exam.getName());
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Khối lớp: " + exam.getGrade());
                contentStream.showText("      Môn học: " + exam.getSubject());
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Thời gian làm bài: " + exam.getDuration() + " phút");
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Đề số: 00" + (exam.getExamCode() + 1));
                contentStream.newLineAtOffset(0, -20);
                contentStream.newLineAtOffset(0, -20);

                contentStream.setFont(boldFont, 12);
                contentStream.showText("Họ và tên: ..........................................");
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Số báo danh: .....................................");
                contentStream.endText();

                // Questions
                contentStream.setFont(font, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 580); // Set the initial position for the first line

                // Create a variable to keep track of the current y-coordinate
                float currentY = 580; // Initial position for the first line

                List<Question> questions = exam.getQuestions();
                for (int i = 0; i < questions.size(); i++) {
                    Question question = questions.get(i);
                    String fullQuestionText = "Câu " + (i + 1) + ": " + question.getText();

                    // Adjust the width as needed
                    List<String> lines = splitTextManually(fullQuestionText, font, 12, 400);
                    for (String line : lines) {
                        // If the current Y-coordinate goes beyond the page boundary, create a new page
                        if (currentY < 50) {
                            contentStream.endText();
                            contentStream.close();
                            page = new PDPage(PDRectangle.A4);
                            document.addPage(page);
                            contentStream = new PDPageContentStream(document, page);
                            contentStream.setFont(font, 12);
                            contentStream.beginText();
                            contentStream.newLineAtOffset(50, 800);
                            currentY = 800; // Reset the Y-coordinate for the new page
                        }
                        contentStream.showText(line);
                        contentStream.newLineAtOffset(0, -20); // next line
                        currentY -= 20;
                    }

                    if (question.getQuestionType().equals("type-in")) {
                        contentStream.showText("Đáp án: ");
                        contentStream.newLineAtOffset(0, -20);
                    }

                    contentStream.newLineAtOffset(10, 0);
                    for (int j = 0; j < question.getChoices().size(); j++) {
                        if (currentY < 50) {
                            contentStream.endText();
                            contentStream.close();
                            page = new PDPage(PDRectangle.A4);
                            document.addPage(page);
                            contentStream = new PDPageContentStream(document, page);
                            contentStream.setFont(font, 12);
                            contentStream.beginText();
                            contentStream.newLineAtOffset(50, 800);
                            currentY = 800; // Reset the Y-coordinate for the new page
                        }
                        // Add each choice
                        contentStream.showText(labelGenerator.getLabel(j) + " " + question.getChoices().get(j));
                        contentStream.newLineAtOffset(0, -20);
                        currentY -= 20;
                    }
                    contentStream.newLineAtOffset(-10, 0);
                    currentY -= 20;

                    if (question.getQuestionType().equals("ranking")) {
                        String rankingTxt = "Thứ tự:   ";
                        for (int j = 1; j <= question.getChoices().size(); j++) {
                            rankingTxt = rankingTxt + "(" + j + ")_____";
                            if (j != question.getChoices().size())
                                rankingTxt = rankingTxt + "  -  ";
                        }
                        contentStream.showText(rankingTxt);
                    }
                    contentStream.newLineAtOffset(0, -20);
                    currentY -= 20;
                }

                contentStream.endText();
                contentStream.close();

                // Save the individual PDF to a temporary file
                File pdfFile = new File(tempDir, exam.getName() + "_" + exam.getExamCode() + ".pdf");
                document.save(pdfFile);

                // Close the individual PDF document
                document.close();

                // Add the individual PDF to the ZIP file
                ZipEntry zipEntry = new ZipEntry(exam.getName() + "_" + exam.getExamCode() + ".pdf");
                zipOutputStream.putNextEntry(zipEntry);

                // Create input stream for each pdf
                FileInputStream fileInputStream = new FileInputStream(pdfFile);

                // Copy into zip file
                IOUtils.copy(fileInputStream, zipOutputStream);

                // Close input stream and current zip output stream
                fileInputStream.close();
                zipOutputStream.closeEntry();

            }

            // Close the ZIP output stream
            zipOutputStream.close();

            // Send the ZIP file as the response
            try (OutputStream outputStream = response.getOutputStream()) {
                FileUtils.copyFile(zipFile, outputStream);
                outputStream.flush();
            }

            // Delete the temporary directory and files
            FileUtils.deleteDirectory(tempDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/export-xls")
    public void exportAnswerXls(
            HttpServletResponse response,
            @RequestParam("examId") String examId) {

        try (Workbook workbook = new HSSFWorkbook()) {

            List<Integer> examCodes = examRepository.findDistinctExamCode(examId);

            for (int k = 0; k < examCodes.size(); k++) {

                int examCode = examCodes.get(k);
                // Generate individual PDFs for each exam
                Exam exam = examRepository.findByExamIdAndCode(examId, examCode);
                List<Question> questions = exam.getQuestions();
                int nQuestion = questions.size();

                Sheet sheet = workbook.createSheet("Đáp án đề " + (examCode + 1));

                Font boldFont = workbook.createFont();
                boldFont.setBold(true);

                CellStyle writeInBold = workbook.createCellStyle();
                writeInBold.setFont(boldFont);

                CellStyle writeInBoldAndCenter = workbook.createCellStyle();
                writeInBoldAndCenter.setFont(boldFont);
                writeInBoldAndCenter.setAlignment(HorizontalAlignment.CENTER);

                CellStyle writeInCenter = workbook.createCellStyle();
                writeInCenter.setAlignment(HorizontalAlignment.CENTER);

                CellStyle writeInLeft = workbook.createCellStyle();
                writeInLeft.setAlignment(HorizontalAlignment.LEFT);

                Row row0 = sheet.createRow(0);
                Cell row0col0 = row0.createCell(0);
                row0col0.setCellValue("Câu");
                row0col0.setCellStyle(writeInBoldAndCenter);
                Cell row0col1 = row0.createCell(1);
                row0col1.setCellValue("Đáp án");
                row0col1.setCellStyle(writeInBoldAndCenter);
                Cell row0col2 = row0.createCell(2);
                row0col2.setCellValue("Chi tiết");
                row0col2.setCellStyle(writeInBoldAndCenter);

                for (int j = 0; j < nQuestion; j++) {

                    Row row = sheet.createRow(j + 1);

                    Cell col0 = row.createCell(0);
                    col0.setCellValue(j + 1);
                    col0.setCellStyle(writeInCenter);

                    Question question = questions.get(j);
                    List<String> answer = question.getAnswer();
                    List<String> choices = question.getChoices();
                    Cell col1 = row.createCell(1);
                    String cellValue = "";
                    if (question.getQuestionType().equals("multiple-choice")) {
                        for (int i = 0; i < choices.size(); i++) {
                            if (answer.contains(choices.get(i))) {
                                if (!cellValue.equals(""))
                                    cellValue = cellValue + " " + getSymbol(i + 1);
                                else
                                    cellValue = cellValue + getSymbol(i + 1);
                            }
                        }
                    } else if (question.getQuestionType().equals("ranking")) {
                        String seq = answer.get(0);
                        int last = 0;
                        for (int i = 0; i < seq.length() - 1; i++) {
                            if (seq.charAt(i) == '-' && seq.charAt(i + 1) == '-') {
                                String curChoice = seq.substring(last, i - 1);
                                for (int choiceIndex = 0; choiceIndex < choices.size(); choiceIndex++) {
                                    String choice = choices.get(choiceIndex);
                                    if (curChoice.equals(choice)) {
                                        if (cellValue.equals(""))
                                            cellValue = cellValue + getSymbol(choiceIndex + 1);
                                        else
                                            cellValue = cellValue + " " + getSymbol(choiceIndex + 1);
                                    }
                                }
                                last = i + 4;
                                i += 3;
                            }
                        }
                        String curChoice = seq.substring(last, seq.length());
                        for (int choiceIndex = 0; choiceIndex < choices.size(); choiceIndex++) {
                            String choice = choices.get(choiceIndex);
                            if (curChoice.equals(choice)) {
                                cellValue = cellValue + " " + getSymbol(choiceIndex + 1);
                            }
                        }
                    } else if (question.getQuestionType().equals("type-in")) {
                        cellValue = answer.get(0);
                    }

                    col1.setCellValue(cellValue);
                    col1.setCellStyle(writeInCenter);

                    if (!question.getQuestionType().equals("type-in")) {
                        Cell col2 = row.createCell(2);
                        if (answer.size() == 1)
                            col2.setCellValue(answer.get(0));
                        else
                            col2.setCellValue(answer.toString());
                        col2.setCellStyle(writeInLeft);
                    }
                }

                for (int columnIndex = 0; columnIndex <= 2; columnIndex++) {
                    sheet.autoSizeColumn(columnIndex);
                }
            }

            // Set the content type and headers for the response
            response.setContentType("application/vnd.ms-excel");
            String fileName = convertVietnameseToLatin(examRepository.findByExamIdAndCode(examId, 0).getName())
                    + "_DAP AN";
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xls");
            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (

        IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> splitTextManually(String text, PDType0Font font, float fontSize, float maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        float lineWidth = 0;

        for (String word : words) {
            try {
                float wordWidth = font.getStringWidth(word) / 1000 * fontSize;
                if (lineWidth + wordWidth > maxWidth) {
                    lines.add(line.toString());
                    line = new StringBuilder();
                    lineWidth = 0;
                }
                line.append(word).append(" ");
                lineWidth += wordWidth;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        lines.add(line.toString());
        return lines;
    }

    public static char getSymbol(int x) {
        return (char) (x + 'A' - 1);
    }

    public static String convertVietnameseToLatin(String input) {
        if (input == null || input.isEmpty()) {
            return input; // Return original string if null or empty
        }

        // Remove diacritics from Vietnamese characters
        String normalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        normalizedString = pattern.matcher(normalizedString).replaceAll("");

        // Convert remaining special Vietnamese characters to Latin equivalents
        normalizedString = normalizedString.replaceAll("[đĐ]", "d");
        normalizedString = normalizedString.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a");
        // Add more replacements for other characters as needed...

        return normalizedString;
    }

}
