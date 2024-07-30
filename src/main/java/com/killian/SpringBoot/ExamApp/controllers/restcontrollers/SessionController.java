package com.killian.SpringBoot.ExamApp.controllers.restcontrollers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.killian.SpringBoot.ExamApp.models.ResponseObject;
import com.killian.SpringBoot.ExamApp.models.User;
import com.killian.SpringBoot.ExamApp.repositories.UserRepository;
import com.killian.SpringBoot.ExamApp.services.SessionManagementService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "/api/v1/session")
public class SessionController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SessionManagementService sessionManagementService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/isSessionValid")
    ResponseEntity<ResponseObject> isSessionValid(HttpServletRequest request) {
        // System.out.println("Checking");
        String redisSession = "spring:session:sessions:" + request.getRequestedSessionId();
        Long ttl = redisTemplate.getExpire(redisSession);
        User user = userRepository.findByUsername(sessionManagementService.getUsername()).orElse(null);
        if (ttl == null || ttl <= 0 || user == null) {
            sessionManagementService.clearUserSession();
            sessionManagementService.setMessage("Phiên đăng nhập hết hạn");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
        if (user != null && user.getBanned() == 1) {
            sessionManagementService.clearUserSession();
            sessionManagementService.setMessage("Tài khoản của bạn đã bị khóa");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(null);
    }

    @GetMapping("/isUserAuthorized")
    ResponseEntity<ResponseObject> isUserAuthorized(
            HttpServletRequest request,
            @RequestParam("pagePermission") String pagePermission) {
        String currentUserPermission = sessionManagementService.getRole();
        if (pagePermission.equals("admin")) {
            if (!currentUserPermission.equals("Admin")) {
                sessionManagementService.setMessage("Cấm truy cập");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
        } else if (pagePermission.equals("teacher"))
            if (currentUserPermission.equals("Student")) {
                sessionManagementService.setMessage("Cấm truy cập");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
        return ResponseEntity.status(HttpStatus.OK)
                .body(null);
    }
}
