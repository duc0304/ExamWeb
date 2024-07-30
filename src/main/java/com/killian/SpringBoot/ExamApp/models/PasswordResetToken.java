package com.killian.SpringBoot.ExamApp.models;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tblPasswordResetToken")
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", columnDefinition = "VARCHAR(255) COLLATE utf8mb4_bin")
    private String email;

    private String expiredTime;

    @Column(name = "tokenId", columnDefinition = "VARCHAR(255) COLLATE utf8mb4_bin")
    private String tokenId;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static SecureRandom random = new SecureRandom();

    public PasswordResetToken() {

    }

    public PasswordResetToken(String email) {
        this.email = email;
        this.expiredTime = getExpiredTimeStr();
        this.tokenId = tokenIdGenerate();
    }

    public void setNewToken() {
        this.expiredTime = getExpiredTimeStr();
        this.tokenId = tokenIdGenerate();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(String expiredTime) {
        this.expiredTime = expiredTime;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public static String getExpiredTimeStr() {
        LocalDateTime codeExpiredTime = LocalDateTime.now().plusMinutes(5);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss MM-dd-yyyy");
        String formattedStr = codeExpiredTime.format(formatter);
        return formattedStr;
    }

    public static String tokenIdGenerate() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(randomIndex));
        }
        return code.toString();
    }
}
