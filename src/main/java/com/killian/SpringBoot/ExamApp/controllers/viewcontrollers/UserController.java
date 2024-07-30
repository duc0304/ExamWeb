package com.killian.SpringBoot.ExamApp.controllers.viewcontrollers;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.killian.SpringBoot.ExamApp.models.User;
import com.killian.SpringBoot.ExamApp.repositories.UserRepository;
import com.killian.SpringBoot.ExamApp.services.ImageStorageService;
import com.killian.SpringBoot.ExamApp.services.SessionManagementService;
import com.killian.SpringBoot.ExamApp.services.UserServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@SuppressWarnings("null")
public class UserController {

    @Autowired
    private SessionManagementService sessionManagementService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private ImageStorageService storageService;

    @GetMapping("/profile")
    public String profile(Model model) {

        User user = userRepository.findByUsername(sessionManagementService.getUsername()).orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("message", sessionManagementService.getMessage());
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String editProfilePage(Model model) {

        User user = userRepository.findByUsername(sessionManagementService.getUsername()).orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("message", sessionManagementService.getMessage());
        model.addAttribute("role", sessionManagementService.getRole());
        sessionManagementService.clearMessage();
        return "edit-profile";
    }

    @PostMapping("edit-profile")
    public String editProfile(
            @RequestParam("avatar") MultipartFile avatar,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("gender") String gender,
            @RequestParam("dob") String dob) {

        User thisUser = userRepository.findByUsername(sessionManagementService.getUsername()).get();
        User user = userRepository.findByEmail(email).get();
        if (user != null && user.getUsername() != thisUser.getUsername()) {
            sessionManagementService.setMessage("Email đã được đăng kí bởi tài khoản khác.");
            return "redirect:/profile/edit";
        }
        if (dob != null) {
            DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = LocalDate.parse(dob, inputFormat);
            DateTimeFormatter desiredFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            String formattedDob = date.format(desiredFormat);
            thisUser.setDob(formattedDob);
        }
        if (email != null)
            thisUser.setEmail(email);
        if (gender != null)
            thisUser.setGender(gender);
        if (name != null)
            thisUser.setName(name);
        if (!avatar.isEmpty()) {
            try {
                String oldFileName = userRepository.findByUsername(sessionManagementService.getUsername()).get()
                        .getAvatarFileName();
                if (oldFileName != null && !oldFileName.equals("default.jpg"))
                    storageService.deleteFile(oldFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String generatedFileName = storageService.storeFile(avatar);
            thisUser.setAvatarFileName(generatedFileName);
            sessionManagementService.setAvatarFileName(generatedFileName);
        }
        userRepository.save(thisUser);
        sessionManagementService.setMessage("Cập nhật thông tin thành công");
        return "redirect:/profile";
    }

    @GetMapping("/change-password-page")
    public String changePasswordPage(Model model) {
        model.addAttribute("message", sessionManagementService.getMessage());
        model.addAttribute("role", sessionManagementService.getRole());
        sessionManagementService.clearMessage();
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {
        User user = userRepository.findByUsername(sessionManagementService.getUsername()).get();
        if (userService.correctPassword(user, currentPassword)) {
            if (newPassword.equals(confirmPassword)) {
                sessionManagementService.setMessage("Đổi mật khẩu thành công.");
                userService.changePassword(user, newPassword);
            } else {
                sessionManagementService.setMessage("Mật khẩu xác nhận không đúng.");
            }
        } else
            sessionManagementService.setMessage("Mật khẩu hiện tại không đúng");
        return "redirect:/change-password-page";
    }

    @GetMapping("/back-to-dashboard")
    public String backToDashboard() {
        if (sessionManagementService.getUsername() == null)
            return "redirect:/";
        return "redirect:/" + sessionManagementService.getRole().toLowerCase() + "/dashboard";
    }

    @GetMapping("/teacher/dashboard")
    public String teacherDashboard(Model model) {
        // Retrieve user data from the session
        String username = sessionManagementService.getUsername();
        User user = null;
        if (username.contains("@")) {
            user = userRepository.findByEmail(username).orElse(null);
            sessionManagementService.setUsername(user.getUsername());
        } else
            user = userRepository.findByUsername(username).orElse(null);
        String role = sessionManagementService.getRole();

        // Use the data as needed
        model.addAttribute("name", user.getName());
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("message", sessionManagementService.getMessage());
        sessionManagementService.clearMessage();
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "teacher/teacher-dashboard";
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard(Model model) {
        String username = sessionManagementService.getUsername();
        User user = null;
        if (username.contains("@")) {
            user = userRepository.findByEmail(username).orElse(null);
            sessionManagementService.setUsername(user.getUsername());
        } else
            user = userRepository.findByUsername(username).orElse(null);
        String role = sessionManagementService.getRole();

        // Use the data as needed
        model.addAttribute("name", user.getName());
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("message", sessionManagementService.getMessage());
        sessionManagementService.clearMessage();
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "student/student-dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        String username = sessionManagementService.getUsername();
        User user = null;
        if (username.contains("@")) {
            user = userRepository.findByEmail(username).orElse(null);
            sessionManagementService.setUsername(user.getUsername());
        } else
            user = userRepository.findByUsername(username).orElse(null);
        String role = sessionManagementService.getRole();

        // Use the data as needed
        model.addAttribute("name", user.getName());
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("message", sessionManagementService.getMessage());
        sessionManagementService.clearMessage();
        model.addAttribute("avatarFileName", sessionManagementService.getAvatarFileName());
        return "admin/admin-dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletRequest request) {
        session.invalidate(); // Invalidate the session
        // Redirect to the specified URL after logout
        // String referer = request.getHeader("referer"); // Get the previous URL before
        // logout
        return "redirect:/";
    }

    @GetMapping("/401")
    public String unauthorized() {
        return "401";
    }

    @GetMapping("/403")
    public String forbidden() {
        return "403";
    }

    @GetMapping("/404")
    public String notFound() {
        return "error";
    }
}
