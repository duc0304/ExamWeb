package com.killian.SpringBoot.ExamApp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;

@Service
@SuppressWarnings("null")
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmail(String to, String subject, String body) throws MessagingException {
        jakarta.mail.internet.MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        // Set the recipient, subject, and body of the email
        try {
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // Set the second parameter to true to enable HTML content
        } catch (jakarta.mail.MessagingException e) {
            e.printStackTrace();
        }
        // Send the email
        javaMailSender.send(message);
    }
}
