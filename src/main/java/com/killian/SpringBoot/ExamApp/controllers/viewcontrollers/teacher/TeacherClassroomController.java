package com.killian.SpringBoot.ExamApp.controllers.viewcontrollers.teacher;

import java.io.IOException;
import java.io.OutputStream;
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
import com.killian.SpringBoot.ExamApp.models.StudentClassroom;
import com.killian.SpringBoot.ExamApp.models.User;
import com.killian.SpringBoot.ExamApp.repositories.AssignmentRepository;
import com.killian.SpringBoot.ExamApp.repositories.ClassroomRepository;
import com.killian.SpringBoot.ExamApp.repositories.QuestionRepository;
import com.killian.SpringBoot.ExamApp.repositories.StudentClassroomRepository;
import com.killian.SpringBoot.ExamApp.repositories.SubmissionRepository;
import com.killian.SpringBoot.ExamApp.repositories.UserRepository;
import com.killian.SpringBoot.ExamApp.services.SessionManagementService;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@SuppressWarnings("null")
@RequestMapping(path = "/teacher/classroom")
public class TeacherClassroomController {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SessionManagementService sessionManagementService;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private StudentClassroomRepository studentClassroomRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/classrooms-page")
    public String classroomPage(Model model) {
        List<Classroom> classrooms = classroomRepository.findByTeacher(sessionManagementService.getUsername());
        List<String> classNames = classrooms.stream()
                .map(Classroom::getName)
                .collect(Collectors.toList());
        List<String> classCodes = classrooms.stream()
                .map(Classroom::getClassCode)
                .collect(Collectors.toList());
        model.addAttribute("classNames", classNames);
        model.addAttribute("classCodes", classCodes);
        model.addAttribute("message", sessionManagementService.getMessage());
        sessionManagementService.clearMessage();
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "teacher/classrooms";
    }

    @GetMapping("/view-classroom")
    public String viewClassroom(
            @RequestParam("classCode") String classCode,
            Model model) {
        Classroom classroom = classroomRepository.findByClasscode(classCode);
        model.addAttribute("classroom", classroom);
        model.addAttribute("message", sessionManagementService.getMessage());
        sessionManagementService.clearMessage();
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "teacher/view-classroom";
    }

    @PostMapping("/remove-classroom")
    public String removeClassroom(
            @RequestParam("classCode") String classCode) {
        List<StudentClassroom> studentClassrooms = studentClassroomRepository.findAllRecordByClasscode(classCode);
        studentClassroomRepository.deleteAll(studentClassrooms);
        List<Assignment> assignments = assignmentRepository.findAssignmentsByClasscode(classCode);
        for (Assignment assignment : assignments) {
            submissionRepository.deleteByAssignmentId(assignment.getAssignmentId());
            assignmentRepository.deleteByAssignmentId(assignment.getAssignmentId());
        }
        Classroom classroom = classroomRepository.findByClasscode(classCode);
        sessionManagementService.setMessage("Bạn đã xóa lớp " + classroom.getName());
        classroomRepository.delete(classroom);
        return "redirect:/teacher/classroom/classrooms-page";
    }

    @GetMapping("/export-student-list")
    public void exportStudentList(
            HttpServletResponse response,
            @RequestParam("classCode") String classCode,
            Model model) {
        Classroom classroom = classroomRepository.findByClasscode(classCode);
        List<String> usernames = studentClassroomRepository.findAllStudentsByClasscode(classCode);
        List<User> users = new ArrayList<>();
        for (String username : usernames) {
            User user = userRepository.findByUsername(username).orElse(null);
            users.add(user);
        }
        try (
                // Create a new Excel workbook and sheet
                Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh sách học sinh");

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
            row0col1.setCellValue("Mã lớp: " + classCode);
            row0col1.setCellStyle(writeInBold);

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
            row2col3.setCellValue("Ngày sinh");
            row2col3.setCellStyle(writeInBoldAndCenter);

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
                cell3.setCellValue(users.get(i).getDob());
                cell3.setCellStyle(writeInCenter);
            }

            for (int columnIndex = 0; columnIndex <= 3; columnIndex++) {
                sheet.autoSizeColumn(columnIndex);
            }

            // Set the content type and headers for the response
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-disposition", "attachment; filename=danh_sach_hoc_sinh.xls");

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

    @GetMapping("/student-list")
    public String studentList(
            @RequestParam("classCode") String classCode,
            Model model) {
        List<String> usernames = studentClassroomRepository.findAllStudentsByClasscode(classCode);
        List<User> students = userRepository.findAllByUsernames(usernames);
        String className = classroomRepository.findByClasscode(classCode).getName();
        model.addAttribute("className", className);
        model.addAttribute("classCode", classCode);
        model.addAttribute("students", students);
        model.addAttribute("message", sessionManagementService.getMessage());
        sessionManagementService.clearMessage();
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "teacher/student-list";
    }

    @PostMapping("/add-student")
    public String addStudent(
            @RequestParam("student") String student,
            @RequestParam("classCode") String classCode) {
        List<String> students = studentClassroomRepository.findAllStudentsByClasscode(classCode);
        String className = classroomRepository.findByClasscode(classCode).getName();
        if (students.contains(student)) {
            sessionManagementService.setMessage("Học sinh hiện đã ở trong lớp");
        } else {
            if (userRepository.existsByUsername(student)) {
                sessionManagementService.setMessage("Thêm học sinh thành công");
                studentClassroomRepository
                        .save(new StudentClassroom(student, className, classCode));
            } else {
                sessionManagementService.setMessage("Không tồn tại người dùng!");
            }
        }
        return "redirect:/teacher/classroom/student-list?classCode=" + classCode;
    }

    @PostMapping("/remove-student")
    public String removeStudent(
            @RequestParam("name") String name,
            @RequestParam("classCode") String classCode) {
        StudentClassroom studentClassroom = studentClassroomRepository.findRecord(name, classCode);
        studentClassroomRepository.delete(studentClassroom);
        return "redirect:/teacher/classroom/student-list?classCode=" + classCode;
    }

    @GetMapping("/create-classroom-page")
    public String createClassroomPage(Model model) {
        List<String> subjects = questionRepository.findDistinctSubjects();
        List<Integer> grades = questionRepository.findDistinctGrades();
        model.addAttribute("subjects", subjects);
        model.addAttribute("grades", grades);
        model.addAttribute("message", sessionManagementService.getMessage());
        sessionManagementService.clearMessage();
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "teacher/create-classroom";
    }

    @PostMapping("/create-classroom")
    public String createClassroom(
            @RequestParam String name,
            @RequestParam String selectedSubject,
            @RequestParam String selectedGrade,
            Model model) {
        String owner = sessionManagementService.getUsername();
        Classroom newClassroom = new Classroom(name, selectedSubject, selectedGrade,
                owner);
        Classroom classroom = classroomRepository.findByNameAndTeacher(name, owner);
        if (classroom == null) {
            classroomRepository.save(newClassroom);
            sessionManagementService.setMessage("Tạo lớp thành công");
            return "redirect:/teacher/classroom/view-classroom?classCode=" + newClassroom.getClassCode();
        } else {
            sessionManagementService.setMessage("Tên lớp đã tồn tại");
            return "redirect:/teacher/classroom/create-classroom-page";
        }
    }
}
