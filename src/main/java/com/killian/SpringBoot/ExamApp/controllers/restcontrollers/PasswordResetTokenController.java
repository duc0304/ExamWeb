package com.killian.SpringBoot.ExamApp.controllers.restcontrollers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.killian.SpringBoot.ExamApp.models.PasswordResetToken;
import com.killian.SpringBoot.ExamApp.models.User;
import com.killian.SpringBoot.ExamApp.repositories.PasswordResetTokenRepository;
import com.killian.SpringBoot.ExamApp.repositories.UserRepository;
import com.killian.SpringBoot.ExamApp.services.EmailService;

@RestController
@RequestMapping(path = "/api/auth")

public class PasswordResetTokenController {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    @PostMapping("/forget-password")
    public String sendTokenToEmail(@RequestParam("email") String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return "Email chưa đăng ký tài khoản.";
        }
        try {
            PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByEmail(email);
            if (passwordResetToken == null) {
                passwordResetToken = new PasswordResetToken(email);
            }
            passwordResetToken.setNewToken();
            passwordResetTokenRepository.save(passwordResetToken);
            String emailBody = "Mã lấy lại mật khẩu của bạn: " + passwordResetToken.getTokenId()
                    + "<br>" + "Mã có thời hạn 5 phút. Vui lòng không chia sẻ nhằm bảo vệ tài khoản cá nhân.";
            emailService.sendEmail(email, "Quên mật khẩu TQBEdu", emailBody);
            return "Mã xác nhận đã được gửi về email của bạn.";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/check-token")
    public String checkToken(@RequestParam String tokenId) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByTokenId(tokenId);
        if (passwordResetToken == null) {
            return "Mã không hợp lệ";
        }
        String expiredTimeStr = passwordResetToken.getExpiredTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss MM-dd-yyyy");
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime expiredTime = LocalDateTime.parse(expiredTimeStr, formatter);
        if (expiredTime.isBefore(localDateTime)) {
            return "Mã đã hết hạn. Hãy gửi lại mã mới.";
        } else {
            return "OK.";
        }
    }

}
