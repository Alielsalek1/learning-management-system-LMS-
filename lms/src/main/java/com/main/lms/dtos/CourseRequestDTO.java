package com.main.lms.dtos;


import lombok.Data;

@Data
public class CourseRequestDTO {
    private String title;
    private String duration;
    private String description;

    public CourseRequestDTO() {
    }
}
