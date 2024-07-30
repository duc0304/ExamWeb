package com.killian.SpringBoot.ExamApp.controllers.restcontrollers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.killian.SpringBoot.ExamApp.models.Classroom;
import com.killian.SpringBoot.ExamApp.repositories.ClassroomRepository;

@RestController
@RequestMapping(path = "/api/classroom")

public class ClassroomController {

    // Dependency Injection
    @Autowired
    private ClassroomRepository classroomRepository;

    @GetMapping("/check-classroom")
    public String checkClassroom(@RequestParam String classCode) {
        Classroom classroom = classroomRepository.findByClasscode(classCode);
        if (classroom != null) {
            return "Tồn tại lớp học.";
        } else {
            return "Không tồn tại lớp học trong hệ thống. Hãy kiểm tra lại mã lớp.";
        }
    }
}
