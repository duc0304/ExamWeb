package com.killian.SpringBoot.ExamApp.controllers.viewcontrollers.admin;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.killian.SpringBoot.ExamApp.models.User;
import com.killian.SpringBoot.ExamApp.repositories.UserRepository;

@Controller
@RequestMapping(path = "/admin/access")
public class AdminAccessController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user-manager")
    public String userManagerPage() {
        return "admin/user-manager";
    }

    @GetMapping("/find-user")
    public String findUser(@RequestParam("keyword") String keyword, Model model) {
        List<User> users = userRepository.findUserThatStartWith(keyword);
        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);
        return "admin/user-manager";
    }

    @PostMapping("/update-user-status")
    public String updateUserStatus(
            @RequestParam("keyword") String keyword,
            @RequestParam("username") String username) {

        User user = userRepository.findByUsername(username).get();
        user.setBanned(1 - user.getBanned());
        userRepository.save(user);

        return "redirect:/admin/access/find-user?keyword=" + keyword;
    }
}
