package com.main.lms.services;

import com.main.lms.dtos.AssignmentResponseDTO;
import com.main.lms.dtos.CreateAssignmentDTO;
import com.main.lms.entities.Assignment;
import com.main.lms.entities.Course;
import com.main.lms.entities.EnrolledCourse;
import com.main.lms.entities.User;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.exceptions.ResourceNotFoundException;
import com.main.lms.repositories.AssignmentRepository;
import com.main.lms.repositories.CourseRepository;
import com.main.lms.repositories.EnrolledCourseRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final NotificationService notificationService;
    private final EnrolledCourseRepository enrolledCourseRepository;

    // 1. Create Assignment
    public AssignmentResponseDTO createAssignment(CreateAssignmentDTO dto, User invoker) {
        // Retrieve course by ID
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + dto.getCourseId()));

        // not instructor of this course
        if (!Objects.equals(course.getInstructor().getId(), invoker.getId())) {
            throw new InvalidUser("You are not the instructor for this course");
        }

        Assignment assignment = new Assignment();
        assignment.setCourse(course);
        assignment.setInstructions(dto.getInstructions());
        assignment.setMaxGrade(dto.getMaxGrade());
        assignment.setCourseId(course.getId());

        List<EnrolledCourse> enrolledStudents = enrolledCourseRepository.findByCourse(course);
        notificationService.notifyUser(course.getInstructor().getId(),
                "New Assignment Created Successfully for course " + course.getTitle());
        for (EnrolledCourse enrolledCourse : enrolledStudents) {
            notificationService.notifyUser(enrolledCourse.getStudent().getId(),
                    "New Assignment has been posted for course " + course.getTitle());
        }

        return AssignmentResponseDTO.mapToResponseDTO(assignmentRepository.save(assignment));
    }

    // 3. Get Assignment by ID
    public AssignmentResponseDTO getAssignmentById(Long id) {
        return AssignmentResponseDTO.mapToResponseDTO(assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with ID: " + id)));
    }

    // 4. Update Assignment
    public void updateAssignment(Long id, CreateAssignmentDTO dto, User invoker) {
        Assignment curAssignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with ID: " + id));

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + dto.getCourseId()));

        // not instructor of this course
        if (!Objects.equals(course.getInstructor().getId(), invoker.getId())) {
            throw new InvalidUser("You are not the instructor for this course");
        }

        curAssignment.setInstructions(dto.getInstructions());
        curAssignment.setMaxGrade(dto.getMaxGrade());
        curAssignment.setCourseId(course.getId());

        notificationService.notifyUser(course.getInstructor().getId(),
                "Assignment Updated Successfully for course " + course.getTitle());
        List<EnrolledCourse> enrolledStudents = enrolledCourseRepository.findByCourse(course);
        for (EnrolledCourse enrolledCourse : enrolledStudents) {
            notificationService.notifyUser(enrolledCourse.getStudent().getId(),
                    "Assignment Updated Successfully for course " + course.getTitle());
        }

        AssignmentResponseDTO.mapToResponseDTO(assignmentRepository.save(curAssignment));
    }

    // 5. Delete Assignment
    public void deleteAssignment(Long id, User invoker) {
        Assignment curAssignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with ID: " + id));

        Course course = courseRepository.findById(curAssignment.getCourse().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + curAssignment.getCourse().getId()));

        // not instructor of this course
        if (!Objects.equals(course.getInstructor().getId(), invoker.getId())) {
            throw new InvalidUser("You are not the instructor for this course");
        }

        assignmentRepository.delete(curAssignment);
        notificationService.notifyUser(curAssignment.getCourse().getInstructor().getId(),
                "Assignment Deleted Successfully for course " + curAssignment.getCourse().getTitle());
    }

    public List<AssignmentResponseDTO> getAssignmentbyCourseId(long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));

        List<Assignment> cur = assignmentRepository.findByCourse(course);
        List<AssignmentResponseDTO> ret = new ArrayList<>();
        for (Assignment x : cur) {
            ret.add(AssignmentResponseDTO.mapToResponseDTO(x));
        }
        return ret;
    }
}
