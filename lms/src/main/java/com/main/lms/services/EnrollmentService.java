package com.main.lms.services;

import com.main.lms.dtos.EnrollmentRequestDTO;
import com.main.lms.dtos.EnrollmentResponseDTO;
import com.main.lms.entities.Course;
import com.main.lms.entities.EnrolledCourse;
import com.main.lms.entities.User;
import com.main.lms.enums.UserRole;
import com.main.lms.exceptions.CourseNotFoundException;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.exceptions.ResourceNotFoundException;
import com.main.lms.repositories.CourseRepository;
import com.main.lms.repositories.EnrolledCourseRepository;
import com.main.lms.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final EnrolledCourseRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final NotificationService notificationService;

    // Get All Enrollments
    public List<EnrollmentResponseDTO> getAllEnrollments() {
        List<EnrollmentResponseDTO> enrollments = new ArrayList<>();
        for (EnrolledCourse crs : enrollmentRepository.findAll()) {
            enrollments.add(mapResponseDTO(crs));
        }
        return enrollments;
    }

    // Get Enrollment by ID
    public Optional<EnrollmentResponseDTO> getEnrollmentById(Long id) {
        Optional<EnrolledCourse> crs = enrollmentRepository.findById(id);
        if (crs.isEmpty()) {
            throw new CourseNotFoundException("Entity Not Found");
        }
        return Optional.of(mapResponseDTO(crs.get()));
    }

    public Optional<EnrollmentResponseDTO> getEnrollmentByStudentAndCourse(Long userId, Long courseId) {
        List<EnrolledCourse> crs = enrollmentRepository.findByStudentAndCourse(userRepository.findById(userId).get(),
                courseRepository.findById(courseId).get());
        if (crs.isEmpty()) {
            throw new CourseNotFoundException("Entity Not Found");
        }
        return Optional.of(mapResponseDTO(crs.get(0)));

    }

    public List<EnrollmentResponseDTO> getEnrollmentsByStudent(Long userId) {
        List<EnrolledCourse> crs = enrollmentRepository.findByStudent(userRepository.findById(userId).get());
        List<EnrollmentResponseDTO> enrollments = new ArrayList<>();
        for (EnrolledCourse cr : crs) {
            enrollments.add(mapResponseDTO(cr));
        }
        return enrollments;
    }

    public List<EnrollmentResponseDTO> getEnrollmentsByCourse(Long courseId) {
        List<EnrolledCourse> crs = enrollmentRepository.findByCourse(courseRepository.findById(courseId).get());
        List<EnrollmentResponseDTO> enrollments = new ArrayList<>();
        for (EnrolledCourse cr : crs) {
            enrollments.add(mapResponseDTO(cr));
        }
        return enrollments;
    }

    public EnrollmentResponseDTO updateEnrollmentById(Long id, Long userId, EnrollmentRequestDTO enrollmentRequestDTO) {
        Optional<EnrolledCourse> crsOptional = enrollmentRepository.findById(id);
        if (crsOptional.isEmpty()) {
            throw new RuntimeException("Enrollment not found");
        }

        EnrolledCourse crs = crsOptional.get();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == UserRole.STUDENT && !Objects.equals(crs.getStudent().getId(), userId)) {
            throw new InvalidUser("You are not the student of this enrollment");
        }
        if (user.getRole() == UserRole.INSTRUCTOR && !Objects.equals(crs.getCourse().getInstructor().getId(), userId)) {
            throw new InvalidUser("You are not the instructor of this course");
        }

        if (enrollmentRequestDTO.getIsCompleted() != null) {
            crs.setIsCompleted(enrollmentRequestDTO.getIsCompleted());
        }

        EnrolledCourse updatedCrs = enrollmentRepository.save(crs);

        return mapResponseDTO(updatedCrs);
    }

    public EnrollmentResponseDTO addEnrollment(Long courseId, Long studentId) {
        Optional<Course> course = courseRepository.findById(courseId);
        Optional<User> student = userRepository.findById(studentId);
        if (course.isEmpty() || student.isEmpty()) {
            throw new RuntimeException("Entity Not Found");
        }
        if (!student.get().getRole().equals(UserRole.STUDENT)) {
            throw new RuntimeException("Only Students Can Be Enrolled in Courses");
        }
        List<EnrolledCourse> existingEnrollment = enrollmentRepository.findByStudentAndCourse(student.get(),
                course.get());
        if (!existingEnrollment.isEmpty()) {
            throw new RuntimeException("Student is already enrolled in this course");
        }

        EnrolledCourse crs = new EnrolledCourse();
        crs.setStudent(student.get());
        crs.setCourse(course.get());
        crs.setIsConfirmed(true);

        notificationService.notifyUser(student.get().getId(), "You have been enrolled in a the following course: " + course.get().getTitle());
        notificationService.notifyUser(course.get().getInstructor().getId(), "A new student has been enrolled in your course: " + course.get().getTitle());

        return mapResponseDTO(enrollmentRepository.save(crs));
    }

    // Delete Enrollment
    public void deleteEnrollmentById(Long id, Long userId) {
        Optional<EnrolledCourse> crs = enrollmentRepository.findById(id);
        User user = userRepository.findById(userId).get();
        if (crs.isEmpty()) {
            throw new RuntimeException("Entity Not Found");
        }
        if (user.getRole() == UserRole.STUDENT && !Objects.equals(crs.get().getStudent().getId(), userId)) {
            throw new RuntimeException("You are not the student of this enrollment");
        }
        if (user.getRole() == UserRole.INSTRUCTOR && !Objects.equals(crs.get().getCourse().getInstructor().getId(), userId)) {
            throw new RuntimeException("You are not the instructor of this course");
        }

        notificationService.notifyUser(crs.get().getStudent().getId(), "You have been unenrolled from the following course: " + crs.get().getCourse().getTitle());
        notificationService.notifyUser(crs.get().getCourse().getInstructor().getId(), "A student has been unenrolled from your course: " + crs.get().getCourse().getTitle());

        enrollmentRepository.deleteById(id);
    }

    private EnrollmentResponseDTO mapResponseDTO(EnrolledCourse crs) {
        return new EnrollmentResponseDTO(crs.getStudent(),
                crs.getCourse(), crs.getIsConfirmed());
    }
}
