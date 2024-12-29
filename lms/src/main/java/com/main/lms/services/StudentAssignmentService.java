package com.main.lms.services;

import com.main.lms.dtos.CreateStudentAssignmentDTO;
import com.main.lms.dtos.GradeAssignmentDTO;
import com.main.lms.dtos.StudentAssignmentResponseDTO;
import com.main.lms.entities.*;
import com.main.lms.exceptions.CourseNotFoundException;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.repositories.*;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class StudentAssignmentService {

    private final StudentAssignmentRepository studentAssignmentRepository;
    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;

    boolean exists(User user, Assignment assignment) {
        Optional<StudentAssignment> x = studentAssignmentRepository.findByAssignmentAndStudent(assignment, user);
        if (x.isPresent()) {
            return true;
        } else {
            return false;
        }
    }

    public StudentAssignmentResponseDTO createStudentAssignment(CreateStudentAssignmentDTO dto, long UserID) {

        Assignment assignment = assignmentRepository.findById(dto.getAssignmentId())
                .orElseThrow(() -> new RuntimeException("Assignment not found with ID: " + dto.getAssignmentId()));

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + dto.getCourseId()));

        User student = userRepository.findById(UserID)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + UserID));

        if (enrollmentService.getEnrollmentByStudentAndCourse(student.getId(), course.getId()).isEmpty()) {
            throw new CourseNotFoundException("Student is not enrolled in this course");
        }

        if (exists(student, assignment)) {
            throw new RuntimeException("Student is already enrolled in this assignment");
        }

        StudentAssignment studentAssignment = new StudentAssignment();
        studentAssignment.setAssignment(assignment);
        studentAssignment.setCourse(course);
        studentAssignment.setStudent(student);

        studentAssignmentRepository.save(studentAssignment);
        return StudentAssignmentResponseDTO.mapToResponseDTO(studentAssignment);
    }

    public boolean saveStudentSubmissionFile(List<MultipartFile> files, Long id, Long studentId) {
        String path = System.getProperty("user.dir") + "/uploads/" + "students-assignments/";
        File dir = new File(path);

        if (!dir.exists()) {
            dir.mkdirs();
        }
        for (MultipartFile file : files) {
            StudentAssignment submission = studentAssignmentRepository.findById(id).get();
            String fileName = studentId + "_" + submission.getCourse().getTitle().replace(" ", "-") + "_"
                    + file.getOriginalFilename();
            String filePath = path + fileName;
            File dest = new File(filePath);
            submission.getFileNames().add(fileName);
            studentAssignmentRepository.save(submission);
            try {
                file.transferTo(dest);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public List<Path> getStudentSubmissions(Long id, CustomUserDetails user) {
        StudentAssignment submission = studentAssignmentRepository.findById(id).get();
        if (user.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT"))
                && !submission.getStudent().getId().equals(user.getUser().getId())) {
            throw new RuntimeException("You are not authorized to view this submission");
        }
        if (user.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_INSTRUCTOR") &&
                !submission.getAssignment().getCourse().getInstructor().getId().equals(user.getUser().getId()))) {
            throw new RuntimeException("You are not the instructor of this course");
        }
        List<String> fileNames = submission.getFileNames();
        String path = System.getProperty("user.dir") + "/uploads/" + "students-assignments/";
        List<Path> submissions = new ArrayList<>();
        for (String fileName : fileNames) {
            Path submissionPath = new File(path + fileName).toPath();
            submissions.add(submissionPath);
        }
        return submissions;
    }

    public Resource downloadStudentSubmissionsZip(List<Path> paths, Long studentId) {
        try {
            Path tempZip = Files.createTempFile("submissions_" + studentId + "_", ".zip");
            try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(tempZip))) {
                for (Path path : paths) {
                    File file = path.toFile();
                    try (FileInputStream fis = new FileInputStream(file)) {
                        ZipEntry zipEntry = new ZipEntry(file.getName());
                        zipOut.putNextEntry(zipEntry);
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            zipOut.write(buffer, 0, length);
                        }
                    }
                }
            }
            return new FileSystemResource(tempZip.toFile());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create zip file");
        }
    }

    public List<StudentAssignmentResponseDTO> getAllStudentAssignments() {
        List<StudentAssignment> studentAssignments = studentAssignmentRepository.findAll();
        List<StudentAssignmentResponseDTO> studentAssignmentResponseDTOs = new ArrayList<>();
        for (StudentAssignment studentAssignment : studentAssignments) {
            studentAssignmentResponseDTOs.add(StudentAssignmentResponseDTO.mapToResponseDTO(studentAssignment));
        }
        return studentAssignmentResponseDTOs;
    }

    public List<StudentAssignmentResponseDTO> getStudentAssignmentsByCourseId(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));
        List<StudentAssignment> studentAssignments = studentAssignmentRepository.findByCourse(course);
        List<StudentAssignmentResponseDTO> studentAssignmentResponseDTOs = new ArrayList<>();
        for (StudentAssignment studentAssignment : studentAssignments) {
            studentAssignmentResponseDTOs.add(StudentAssignmentResponseDTO.mapToResponseDTO(studentAssignment));
        }
        return studentAssignmentResponseDTOs;
    }

    public StudentAssignmentResponseDTO getStudentAssignmentByCourseIdAndUserId(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));
        User student = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + userId));
        StudentAssignment studentAssignment = studentAssignmentRepository.findByCourseAndStudent(course, student).get();
        return StudentAssignmentResponseDTO.mapToResponseDTO(studentAssignment);
    }

    public StudentAssignmentResponseDTO getStudentAssignmentById(Long id) {
        return StudentAssignmentResponseDTO.mapToResponseDTO(studentAssignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student Assignment not found with ID: " + id)));
    }

    public void deleteStudentAssignment(Long id) {
        Optional<StudentAssignment> studentAssignment = studentAssignmentRepository.findById(id);
        if (studentAssignment.isEmpty()) {
            throw new RuntimeException("Student Assignment not found with ID: " + id);
        }
        studentAssignmentRepository.delete(studentAssignment.get());
    }

    public List<StudentAssignmentResponseDTO> getAssignmentsForStudent(Long studentId) {
        List<StudentAssignment> studentAssignments = studentAssignmentRepository.findByStudentId(studentId);
        List<StudentAssignmentResponseDTO> studentAssignmentResponseDTOs = new ArrayList<>();
        for (StudentAssignment studentAssignment : studentAssignments) {
            studentAssignmentResponseDTOs.add(StudentAssignmentResponseDTO.mapToResponseDTO(studentAssignment));
        }
        return studentAssignmentResponseDTOs;
    }

    public StudentAssignmentResponseDTO gradeStudentAssignmentAndAddFeedback(Long studentAssignmentId,
            GradeAssignmentDTO dto, long userId) {
        StudentAssignment studentAssignment = studentAssignmentRepository.findById(studentAssignmentId)
                .orElseThrow(
                        () -> new RuntimeException("Student Assignment not found with ID: " + studentAssignmentId));

        if (!Objects.equals(studentAssignment.getCourse().getInstructor().getId(), userId)) {
            throw new InvalidUser("You are not the instructor of this course");
        }

        // Update the student assignment
        studentAssignment.setGrade(dto.getGrade());
        studentAssignment.setFeedback(dto.getFeedback());

        return StudentAssignmentResponseDTO.mapToResponseDTO(studentAssignmentRepository.save(studentAssignment));
    }
}
