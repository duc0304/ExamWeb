package com.killian.SpringBoot.ExamApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.killian.SpringBoot.ExamApp.models.Choice;

public interface ChoiceRepository extends JpaRepository<Choice, Long> {

}
