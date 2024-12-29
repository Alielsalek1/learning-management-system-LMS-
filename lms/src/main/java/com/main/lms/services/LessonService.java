package com.main.lms.services;

import com.main.lms.dtos.LessonRequestDTO;
import com.main.lms.entities.Course;
import com.main.lms.entities.Lesson;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.repositories.CourseRepository;
import com.main.lms.repositories.LessonRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final NotificationService notificationService;


    public Lesson createLesson(LessonRequestDTO lessonRequestDTO, Long userId) {
        // Retrieve course by ID
        Course course = courseRepository.findById(lessonRequestDTO.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + lessonRequestDTO.getCourseId()));

        if(!course.getInstructor().getId().equals(userId)) {
            throw new InvalidUser("You are not authorized to create a lesson for this course");
        }
        Lesson lesson = new Lesson();
        lesson.setOtp(lessonRequestDTO.getOtp());
        lesson.setCourse(course);

        notificationService.notifyUser(course.getInstructor().getId(), "New Lesson Created Successfully for course " + course.getTitle());

        return lessonRepository.save(lesson);
    }

    // Retrieve all lessons
    public List<Lesson> getAllLessons() {
        return lessonRepository.findAll();
    }

    // Retrieve a single lesson by ID
    public Lesson getLessonById(Long id) {
        Optional<Lesson> lesson = lessonRepository.findById(id);
        return lesson.orElseThrow(() -> new RuntimeException("Lesson not found with ID: " + id));
    }

    // Update a lesson
    public Lesson updateLesson(Long id, LessonRequestDTO updatedLesson, long userId) {
        Lesson existingLesson = getLessonById(id);

        // not the instructor of this course
        if (existingLesson.getCourse().getInstructor().getId() != userId) {
            throw new InvalidUser("You are not authorized to update a lesson for this course");
        }

        // Update the otp
        existingLesson.setOtp(updatedLesson.getOtp());

        // Update the course reference
        Course course = courseRepository.findById(updatedLesson.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + updatedLesson.getCourseId()));

//        if(!course.getInstructor().getId().equals(userId)) {
//            throw new RuntimeException("You are not authorized to create a lesson for this course");
//        }

        existingLesson.setCourse(course);

        notificationService.notifyUser(course.getInstructor().getId(), "Lesson Updated Successfully for course " + course.getTitle());

        return lessonRepository.save(existingLesson);
    }

    public List<Lesson> getLessonsByCourseId(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + id));
        return lessonRepository.findByCourse(course);
    }

//    // Delete a lesson by ID
//    public void deleteLesson(Long id) {
//        Lesson lesson = getLessonById(id);
//        lessonRepository.delete(lesson);
//        notificationService.notifyUser(lesson.getCourse().getInstructor().getId(), "Lesson Deleted Successfully for course " + lesson.getCourse().getTitle());
//    }
}
