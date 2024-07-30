package com.killian.SpringBoot.ExamApp.services;

import com.killian.SpringBoot.ExamApp.models.User;

public interface UserService {

    User saveUser(User user);

    void saveRoleToUser(String username, String role);
}
