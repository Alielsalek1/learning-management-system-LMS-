package com.main.lms.services;

import com.main.lms.dtos.CourseRequestDTO;
import com.main.lms.dtos.CourseResponseDTO;
import com.main.lms.entities.Course;
import com.main.lms.entities.EnrolledCourse;
import com.main.lms.entities.User;
import com.main.lms.enums.UserRole;
import com.main.lms.exceptions.CourseNotFoundException;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.repositories.CourseRepository;
import com.main.lms.repositories.EnrolledCourseRepository;
import com.main.lms.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrolledCourseRepository enrolledCourseRepository;

    // Get All Courses
    public List<CourseResponseDTO> getAllCourses() {
        List<CourseResponseDTO> courses = new ArrayList<>();
        for (Course crs : courseRepository.findAll()) {
            courses.add(mapResponseDTO(crs));
        }
        return courses;
    }

    public CourseResponseDTO updateCourse(Long id, CourseRequestDTO courseRequestDTO, Long instructorId) {
        Course crs = courseRepository.findById(id).get();
        if(crs == null) {
            throw new CourseNotFoundException("Course Not Found");
        }
        if (crs.getInstructor().getId() != instructorId) {
            throw new InvalidUser("You are not the instructor of this course");
        }
        crs.setTitle(courseRequestDTO.getTitle() == null ? crs.getTitle() : courseRequestDTO.getTitle());
        crs.setDescription(courseRequestDTO.getDescription() == null ? crs.getDescription() : courseRequestDTO.getDescription());
        crs.setDuration(courseRequestDTO.getDuration() == null ? crs.getDuration() : courseRequestDTO.getDuration());
        
        crs = courseRepository.save(crs);
        return mapResponseDTO(crs);
    }

    // Get Course by ID
    public CourseResponseDTO getCourseById(Long id) throws CourseNotFoundException {
        Optional<Course> crs = courseRepository.findById(id);
        if (crs.isEmpty())
            throw new CourseNotFoundException("Entity Not Found");
        return mapResponseDTO(crs.get());
    }

    public CourseResponseDTO addCourse(String title, String duration, String description, Long instructorId) {
        Course crs = new Course();
        crs.setTitle(title);
        crs.setDescription(description);
        crs.setDuration(duration);
        Optional<User> instructor = userRepository.findById(instructorId);
        if (instructor.isEmpty()) {
            throw new RuntimeException("Instructor Not Found");
        }
        if (instructor.get().getRole() != UserRole.INSTRUCTOR) {
            throw new RuntimeException("Only Instructors Can Be Added to Course Entity");
        }
        crs.setInstructor(instructor.get());
        crs = courseRepository.save(crs);
        return mapResponseDTO(crs);
    }

    // Delete Course
    public void deleteCourseById(Long id ,Long instructorId) {
        Course crs = courseRepository.findById(id).get();
        if (crs.getInstructor().getId() != instructorId) {
            throw new InvalidUser("You are not the instructor of this course");
        }
        courseRepository.deleteById(id);
    }

    public CourseResponseDTO addMaterials(long id, long instructorId, List<MultipartFile> materials) throws CourseNotFoundException {
        Optional<Course> crs = courseRepository.findById(id);
        if (instructorId != crs.get().getInstructor().getId()) {
            throw new InvalidUser("You are not the instructor of this course");
        }
        if (crs.isEmpty()) {
            throw new CourseNotFoundException("Course Not Found");
        }

        String path = System.getProperty("user.dir") + "/uploads/" + "courses-materials/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        for (MultipartFile material : materials) {
            String fileName = crs.get().getId() + "_" + crs.get().getTitle() + "_" + material.getOriginalFilename();
            String filePath = path + fileName;
            File dest = new File(filePath);

            try {
                material.transferTo(dest);
            } catch (Exception e) {
                e.printStackTrace();
            }

            crs.get().getMaterials().add(fileName);
        }

        courseRepository.save(crs.get());
        return mapResponseDTO(crs.get());
    }

    public List<Path> getCourseMaterials(Long id, Long userId) {
        Course crs = courseRepository.findById(id).get();
        User user = userRepository.findById(userId).get();
        EnrolledCourse enrollment = enrolledCourseRepository.findByStudentAndCourse(user, crs).get(0);
        if(user.getRole() == UserRole.INSTRUCTOR && crs.getInstructor().getId() != userId) {
            throw new InvalidUser("You are not the instructor of this course");
        }
        if(user.getRole() == UserRole.STUDENT && enrollment.getStudent().getId() != userId) {
            throw new InvalidUser("You are not enrolled in this course");
        }

        String path = System.getProperty("user.dir") + "/uploads/" + "courses-materials/";
        List<Path> materials = new ArrayList<>();
        for (String material : crs.getMaterials()) {
            Path materialPath = new File(path + material).toPath();
            materials.add(materialPath);
        }
        return materials;
    }

    public Resource downloadMaterialsZip(List<Path> paths, Long id) {
        try {
            Path tempZip = Files.createTempFile("materials_" + id + "_", ".zip");
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

    public boolean isCourseInstructor(Long courseId, Long instructorId) {
        Course crs = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));
        return Objects.equals(crs.getInstructor().getId(), instructorId);
    }

    private CourseResponseDTO mapResponseDTO(Course crs) {
        CourseResponseDTO dto = new CourseResponseDTO();
        dto.setId(crs.getId());
        dto.setInstructorName(crs.getInstructor().getName());
        dto.setTitle(crs.getTitle());
        dto.setDuration(crs.getDuration());
        dto.setDescription(crs.getDescription());
        return dto;
    }
}
