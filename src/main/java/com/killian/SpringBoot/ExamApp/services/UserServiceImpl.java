package com.killian.SpringBoot.ExamApp.services;

import com.killian.SpringBoot.ExamApp.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.killian.SpringBoot.ExamApp.repositories.UserRepository;

// import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
// @Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public void saveRoleToUser(String username, String role) {
        User user = userRepository.findByUsername(username).get();
        user.setRole(role);
    }

    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public boolean correctPassword(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }
}
