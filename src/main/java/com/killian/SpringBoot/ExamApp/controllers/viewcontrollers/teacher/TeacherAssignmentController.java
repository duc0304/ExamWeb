package com.killian.SpringBoot.ExamApp.controllers.viewcontrollers.teacher;

import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.killian.SpringBoot.ExamApp.models.Assignment;
import com.killian.SpringBoot.ExamApp.models.Classroom;
import com.killian.SpringBoot.ExamApp.models.Submission;
import com.killian.SpringBoot.ExamApp.models.User;
import com.killian.SpringBoot.ExamApp.repositories.AssignmentRepository;
import com.killian.SpringBoot.ExamApp.repositories.ClassroomRepository;
import com.killian.SpringBoot.ExamApp.repositories.ExamRepository;
import com.killian.SpringBoot.ExamApp.repositories.SubmissionRepository;
import com.killian.SpringBoot.ExamApp.repositories.UserRepository;
import com.killian.SpringBoot.ExamApp.services.SessionManagementService;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(path = "/teacher/classroom/assignment")
public class TeacherAssignmentController {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionManagementService sessionManagementService;

    @Autowired
    private ClassroomRepository classroomRepository;

    @GetMapping("")
    public String assignmentList(
            @RequestParam("classCode") String classCode,
            Model model) {
        Classroom classroom = classroomRepository.findByClasscode(classCode);
        List<Assignment> assignments = assignmentRepository.findAssignmentsByClasscode(classCode);
        model.addAttribute("assignments", assignments);
        model.addAttribute("classCode", classCode);
        model.addAttribute("className", classroom.getName());
        model.addAttribute("message", sessionManagementService.getMessage());
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        sessionManagementService.clearMessage();
        return "teacher/assignments";
    }

    @GetMapping("add-assignment-page")
    public String addAssignmentPage(
            @RequestParam("classCode") String classCode,
            Model model) {
        String className = classroomRepository.findByClasscode(classCode).getName();
        model.addAttribute("classCode", classCode);
        model.addAttribute("className", className);
        model.addAttribute("message", sessionManagementService.getMessage());
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        sessionManagementService.clearMessage();
        return "teacher/add-assignment";
    }

    @PostMapping("add-assignment")
    public String addAssignment(
            @RequestParam("assignmentName") String assignmentName,
            @RequestParam("deadline") String deadline,
            @RequestParam("examId") String examId,
            @RequestParam("classCode") String classCode) {

        Classroom classroom = classroomRepository.findByClasscode(classCode);
        String className = classroom.getName();
        if (assignmentRepository.findAssignmentByClasscodeAndName(classCode, assignmentName) == null) {
            if (examRepository.findByExamId(examId).isEmpty()) {
                sessionManagementService.setMessage("Không tìm thấy đề thi với ID đã cung cấp!");
                return "redirect:/teacher/classroom/assignment/add-assignment-page?classCode=" + classCode;
            }
            // 2023-10-27T13:43
            SimpleDateFormat fromUser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            SimpleDateFormat myFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
            String reformattedDeadline;
            try {
                reformattedDeadline = myFormat.format(fromUser.parse(deadline));
                Assignment newAssignment = new Assignment(assignmentName, reformattedDeadline, examId, className,
                        classCode);
                assignmentRepository.save(newAssignment);
                sessionManagementService.setMessage("Thêm bài tập thành công!");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return "redirect:/teacher/classroom/assignment?classCode=" + classCode;
        } else {
            sessionManagementService.setMessage("Trùng tên bài tập!");
            return "redirect:/teacher/classroom/assignment/add-assignment-page?classCode=" + classCode;
        }
    }

    @GetMapping("/view-results")
    public String viewResults(
            @RequestParam("assignmentId") String assignmentId,
            Model model) {
        Assignment assignment = assignmentRepository.findByAssignmentId(assignmentId);
        List<Submission> submissions = submissionRepository.findAllSubmissionsByAssignmentId(assignmentId);
        List<String> usernames = submissions.stream()
                .map(submission -> submission.getStudent())
                .collect(Collectors.toList());
        List<User> users = usernames.stream()
                .map(username -> userRepository.findByUsername(username).get())
                .collect(Collectors.toList());
        List<String> names = users.stream()
                .map(user -> user.getName())
                .collect(Collectors.toList());
        List<Double> scores = submissions.stream()
                .map(submission -> submission.getScore())
                .collect(Collectors.toList());
        List<String> timeToFinish = new ArrayList<>();
        for (Submission submission : submissions) {
            String startedTimeStr = submission.getStartedTime();
            String submittedTimeStr = submission.getSubmittedTime();
            String diff = getLocalDateTimeDiffInString(startedTimeStr, submittedTimeStr);
            timeToFinish.add(diff);
        }
        model.addAttribute("names", names);
        model.addAttribute("scores", scores);
        model.addAttribute("timeToFinish", timeToFinish);
        model.addAttribute("classCode", assignment.getClassCode());
        model.addAttribute("assignmentId", assignmentId);
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "teacher/view-results";
    }

    @GetMapping("/export-result")
    public void exportStudentList(
            HttpServletResponse response,
            @RequestParam("assignmentId") String assignmentId,
            Model model) {
        Assignment assignment = assignmentRepository.findByAssignmentId(assignmentId);
        Classroom classroom = classroomRepository.findByClasscode(assignment.getClassCode());
        List<Submission> submissions = submissionRepository.findAllSubmissionsByAssignmentId(assignmentId);
        List<String> usernames = submissions.stream()
                .map(submission -> submission.getStudent())
                .collect(Collectors.toList());
        List<User> users = usernames.stream()
                .map(username -> userRepository.findByUsername(username).get())
                .collect(Collectors.toList());
        try (
                // Create a new Excel workbook and sheet
                Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Kết quả");

            Font boldFont = workbook.createFont();
            boldFont.setBold(true);

            CellStyle writeInBold = workbook.createCellStyle();
            writeInBold.setFont(boldFont);

            CellStyle writeInBoldAndCenter = workbook.createCellStyle();
            writeInBoldAndCenter.setFont(boldFont);
            writeInBoldAndCenter.setAlignment(HorizontalAlignment.CENTER);

            CellStyle writeInCenter = workbook.createCellStyle();
            writeInCenter.setAlignment(HorizontalAlignment.CENTER);

            Row row0 = sheet.createRow(0);

            Cell row0col0 = row0.createCell(0);
            row0col0.setCellValue("Lớp: " + classroom.getName());
            row0col0.setCellStyle(writeInBold);

            Cell row0col1 = row0.createCell(1);
            row0col1.setCellValue("Mã lớp: " + classroom.getClassCode());
            row0col1.setCellStyle(writeInBold);

            Row row1 = sheet.createRow(1);

            Cell row1col0 = row1.createCell(0);
            row1col0.setCellValue("Bài tập: " + assignment.getName());
            row1col0.setCellStyle(writeInBold);

            Row row2 = sheet.createRow(2);

            Cell row2col0 = row2.createCell(0);
            row2col0.setCellValue("STT");
            row2col0.setCellStyle(writeInBoldAndCenter);

            Cell row2col1 = row2.createCell(1);
            row2col1.setCellValue("Họ và tên");
            row2col1.setCellStyle(writeInBoldAndCenter);

            Cell row2col2 = row2.createCell(2);
            row2col2.setCellValue("Tên đăng nhập");
            row2col2.setCellStyle(writeInBoldAndCenter);

            Cell row2col3 = row2.createCell(3);
            row2col3.setCellValue("Điểm");
            row2col3.setCellStyle(writeInBoldAndCenter);

            Cell row2col4 = row2.createCell(4);
            row2col4.setCellValue("Thời gian làm bài");
            row2col4.setCellStyle(writeInBoldAndCenter);

            for (int i = 0; i < users.size(); i++) {
                Row row = sheet.createRow(i + 3);
                Cell cell0 = row.createCell(0);
                cell0.setCellValue("" + (i + 1));
                cell0.setCellStyle(writeInCenter);

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(users.get(i).getName());
                cell1.setCellStyle(writeInCenter);

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(users.get(i).getUsername());
                cell2.setCellStyle(writeInCenter);

                Cell cell3 = row.createCell(3);
                cell3.setCellValue(submissions.get(i).getScore());
                cell3.setCellStyle(writeInCenter);

                String startedTimeStr = submissions.get(i).getStartedTime();
                String submittedTimeStr = submissions.get(i).getSubmittedTime();
                String diff = getLocalDateTimeDiffInString(startedTimeStr, submittedTimeStr);
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(diff);
                cell4.setCellStyle(writeInCenter);
            }

            for (int columnIndex = 0; columnIndex <= 4; columnIndex++) {
                sheet.autoSizeColumn(columnIndex);
            }

            // Set the content type and headers for the response
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-disposition", "attachment; filename=danh_sach_diem.xls");

            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("remove-assignment")
    public String removeAssignment(
            @RequestParam("assignmentId") String assignmentId,
            @RequestParam("classCode") String classCode) {
        Assignment assignment = assignmentRepository.findByAssignmentId(assignmentId);
        sessionManagementService.setMessage("Đã xóa bài tập: " + assignment.getName());
        assignmentRepository.delete(assignment);
        return "redirect:/teacher/classroom/assignment?classCode=" + classCode;
    }

    @GetMapping("edit-assignment-page")
    public String editAssignmentPage(
            @RequestParam("assignmentId") String assignmentId,
            @RequestParam("classCode") String classCode,
            Model model) {
        Assignment assignment = assignmentRepository.findByAssignmentId(assignmentId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        DateTimeFormatter desiredFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime currentDeadline = LocalDateTime.parse(assignment.getDeadline(), formatter);
        String currentDeadlineStr = currentDeadline.format(desiredFormatter);
        model.addAttribute("assignment", assignment);
        model.addAttribute("deadline", currentDeadlineStr);
        model.addAttribute("classCode", classCode);
        model.addAttribute("message", sessionManagementService.getMessage());
        sessionManagementService.clearMessage();
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "teacher/edit-assignment";
    }

    @PostMapping("edit-assignment")
    public String editAssignment(
            @RequestParam("assignmentId") String assignmentId,
            @RequestParam("assignmentName") String assignmentName,
            @RequestParam("deadline") String deadline,
            @RequestParam("examId") String examId,
            @RequestParam("classCode") String classCode) {

        Assignment assignment = assignmentRepository.findByAssignmentId(assignmentId);
        if (examRepository.findByExamId(examId).isEmpty()) {
            sessionManagementService.setMessage("Không tìm thấy đề thi với ID đã cung cấp!");
            return "redirect:/teacher/classroom/assignment/edit-assignment-page?classCode=" + classCode
                    + "&assignmentId=" + assignmentId;
        }
        Assignment checkAssignment = assignmentRepository.findAssignmentByClasscodeAndName(classCode, assignmentName);
        if (checkAssignment != null && checkAssignment != assignment) {
            sessionManagementService.setMessage("Tên bài tập bị trùng lặp!");
            return "redirect:/teacher/classroom/assignment/edit-assignment-page?classCode=" + classCode
                    + "&assignmentId=" + assignmentId;
        }
        // 2023-10-27T13:43
        SimpleDateFormat fromUser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        SimpleDateFormat myFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        String reformattedDeadline;
        try {
            reformattedDeadline = myFormat.format(fromUser.parse(deadline));
            assignment.setDeadline(reformattedDeadline);
            assignment.setName(assignmentName);
            assignment.setExamId(examId);
            assignmentRepository.save(assignment);
            sessionManagementService.setMessage("Thay đổi thông tin bài tập thành công!");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "redirect:/teacher/classroom/assignment/view-assignment?classCode=" + classCode + "&assignmentId="
                + assignmentId;
    }

    @GetMapping("view-assignment")
    public String viewAssignment(
            @RequestParam("assignmentId") String assignmentId,
            @RequestParam("classCode") String classCode,
            Model model) {

        Assignment assignment = assignmentRepository.findByAssignmentId(assignmentId);
        model.addAttribute("assignment", assignment);
        model.addAttribute("classCode", classCode);
        model.addAttribute("message", sessionManagementService.getMessage());
        sessionManagementService.clearMessage();
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "teacher/view-assignment";
    }

    private static String getLocalDateTimeDiffInString(String str1, String str2) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss MM/dd/yyyy");
        LocalDateTime time1 = LocalDateTime.parse(str1, formatter);
        LocalDateTime time2 = LocalDateTime.parse(str2, formatter);
        int s = (time2.getHour() * 3600 + time2.getMinute() * 60 + time2.getSecond())
                - (time1.getHour() * 3600 + time1.getMinute() * 60 + time1.getSecond());
        int m = s / 60;
        int h = m / 60;

        s = s % 60;
        m = m % 60;
        h = h % 24;

        String hh = (h < 10) ? ("0" + h) : ("" + h);
        String mm = (m < 10) ? ("0" + m) : ("" + m);
        String ss = (s < 10) ? ("0" + s) : ("" + s);

        return hh + ":" + mm + ":" + ss;
    }
}
