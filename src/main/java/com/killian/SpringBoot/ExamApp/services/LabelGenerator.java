package com.killian.SpringBoot.ExamApp.services;

import org.springframework.stereotype.Component;

@Component
public class LabelGenerator {
    public String getLabel(int index) {
        if (index >= 0 && index < 26) {
            return String.valueOf((char) ('A' + index)) + ".";
        }
        return "";
    }
}
