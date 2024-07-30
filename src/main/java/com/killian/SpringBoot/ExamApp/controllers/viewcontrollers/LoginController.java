package com.killian.SpringBoot.ExamApp.controllers.viewcontrollers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.killian.SpringBoot.ExamApp.models.PasswordResetToken;
import com.killian.SpringBoot.ExamApp.models.User;
import com.killian.SpringBoot.ExamApp.repositories.PasswordResetTokenRepository;
import com.killian.SpringBoot.ExamApp.repositories.UserRepository;
import com.killian.SpringBoot.ExamApp.services.SessionManagementService;
import com.killian.SpringBoot.ExamApp.services.UserServiceImpl;

@Controller
public class LoginController {

    @Autowired
    private SessionManagementService sessionManagementService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @GetMapping("/")
    public String login(Model model) {
        if (sessionManagementService.getRole() != null && sessionManagementService.getUsername() != null)
            return "redirect:/" + sessionManagementService.getRole().toLowerCase() + "/dashboard";
        model.addAttribute("message", sessionManagementService.getMessage());
        String username = sessionManagementService.getUsername();
        model.addAttribute("lastUsername", username);
        sessionManagementService.clearUserSession();
        return "login";
    }

    @GetMapping("/login")
    public String login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            Model model) {

        boolean loginWithEmail = username.contains("@");
        User user = null;
        if (loginWithEmail)
            user = userRepository.findByEmail(username).orElse(null);
        else
            user = userRepository.findByUsername(username).orElse(null);
        String message = null;
        if (user == null) {
            message = loginWithEmail ? "Email chưa được đăng kí" : "Tên đăng nhập không tồn tại.";
            sessionManagementService.setMessage(message);
            return "redirect:/";
        } else {
            if (!userService.correctPassword(user, password)) {
                message = "Sai mật khẩu.";
                sessionManagementService.setMessage(message);
                sessionManagementService.setUsername(username);
                return "redirect:/";
            } else {
                sessionManagementService.createUserSession(username, user.getRole(), user.getAvatarFileName());
                return "redirect:/" + user.getRole().toLowerCase() + "/dashboard";
            }
        }
    }

    @GetMapping("/reset-password-page")
    public String resetPasswordPage(@RequestParam("tokenId") String tokenId, Model model) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByTokenId(tokenId);
        String email = passwordResetToken.getEmail();
        User user = userRepository.findByEmail(email).orElse(null);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", email);
        model.addAttribute("tokenId", tokenId);
        model.addAttribute("message", sessionManagementService.getMessage());
        sessionManagementService.clearMessage();
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam("tokenId") String tokenId,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (!password.equals(confirmPassword)) {
            sessionManagementService.setMessage("Mật khẩu xác nhận không chính xác.");
            return "redirect:/reset-password-page?tokenId=" + tokenId;
        }
        userService.changePassword(user, confirmPassword);
        sessionManagementService.setMessage("Đổi mật khẩu thành công");
        return "redirect:/";
    }

    @GetMapping("/forget-password")
    public ModelAndView forget() {
        ModelAndView modelAndView = new ModelAndView("forget-password.html");
        modelAndView.addObject("message", sessionManagementService.getMessage());
        sessionManagementService.clearMessage();
        return modelAndView;
    }

}
