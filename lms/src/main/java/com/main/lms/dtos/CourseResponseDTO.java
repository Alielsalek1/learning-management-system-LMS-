package com.main.lms.dtos;

import java.util.List;

import com.main.lms.entities.Course;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CourseResponseDTO {
    private Long id;
    private String instructorName;
    private String title;
    private String duration;
    private String description;

    public static CourseResponseDTO mapToResponseDTO(Course course) {
        CourseResponseDTO courseResponseDTO = new CourseResponseDTO();
        courseResponseDTO.setId(course.getId());
        courseResponseDTO.setInstructorName(course.getInstructor().getName());
        courseResponseDTO.setTitle(course.getTitle());
        courseResponseDTO.setDuration(course.getDuration());
        courseResponseDTO.setDescription(course.getDescription());
        return courseResponseDTO;
    }
}
