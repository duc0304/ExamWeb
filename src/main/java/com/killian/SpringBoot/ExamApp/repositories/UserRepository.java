package com.killian.SpringBoot.ExamApp.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.killian.SpringBoot.ExamApp.models.User;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = :username")
    boolean existsByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.username IN :userList")
    List<User> findAllByUsernames(@Param("userList") List<String> userList);

    @Query("SELECT u FROM User u WHERE u.username LIKE ?1% AND u.username <> 'admin'")
    List<User> findUserThatStartWith(String keyword);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}
