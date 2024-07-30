package com.killian.SpringBoot.ExamApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.killian.SpringBoot.ExamApp.models.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    @Query("SELECT fpt FROM PasswordResetToken fpt WHERE fpt.tokenId = :tokenId")
    PasswordResetToken findByTokenId(@Param("tokenId") String tokenId);

    @Query("SELECT fpt FROM PasswordResetToken fpt WHERE fpt.email = :email")
    PasswordResetToken findByEmail(@Param("email") String email);
}
