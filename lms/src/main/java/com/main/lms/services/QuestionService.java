package com.main.lms.services;

import com.main.lms.dtos.QuestionRequestDTO;
import com.main.lms.dtos.QuestionResponseDTO;
import com.main.lms.entities.Course;
import com.main.lms.entities.Question;
import com.main.lms.exceptions.CourseNotFoundException;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.exceptions.ResourceNotFoundException;
import com.main.lms.repositories.CourseRepository;
import com.main.lms.repositories.QuestionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.main.lms.enums.QuestionType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final CourseRepository courseRepository;
    private final NotificationService notificationService;



    @Transactional
    public Question createQuestion(QuestionRequestDTO requestDTO, long userId) {
        Course course = courseRepository.findById(requestDTO.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + requestDTO.getCourseId()));

        // not the instructor of the course
        if (course.getInstructor().getId() != userId) {
            throw new InvalidUser("You are not the instructor of this course");
        }

        Question question = new Question();
        question.setCourse(course);
        question.setQuestionContent(requestDTO.getContent());
        question.setType(requestDTO.getType());
        question.setAnswer(requestDTO.getAnswer());

        Question savedQuestion = questionRepository.save(question);

        notificationService.notifyUser(course.getInstructor().getId(), "New Question has been posted for course " + course.getTitle());

        return savedQuestion;
    }

    public Question getQuestionById(Long questionId, long userId) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + questionId));

        Course course = q.getCourse();

        // not the instructor of the course
        if (course.getInstructor().getId() != userId) {
            throw new InvalidUser("You are not the instructor of this course");
        }

        return q;
    }

    public List<Question> getFilteredQuestions(Long courseId, Optional<QuestionType> questionType, long userId) {
        if (!courseRepository.existsById(courseId)) {
            throw new CourseNotFoundException("Course not found with ID: " + courseId);
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        // not the instructor of the course
        if (course.getInstructor().getId() != userId) {
            throw new InvalidUser("You are not the instructor of this course");
        }

        List<Question> questions;
        questions = questionType.map(type -> questionRepository.findByCourseId(courseId)
                .stream()
                .filter(q -> q.getType().equals(type))
                .collect(Collectors.toList())).orElseGet(() -> questionRepository.findByCourseId(courseId));

        return questions;
    }

    public List<Question> getRandomQuestions(Long courseId, Optional<QuestionType> questionType, int count) {
        if (!courseRepository.existsById(courseId)) {
            throw new CourseNotFoundException("Course not found with ID: " + courseId);
        }
        List<Question> filteredQuestions;
        filteredQuestions = questionType.map(type -> questionRepository.findByCourseId(courseId)
                .stream()
                .filter(q -> q.getType().equals(type))
                .collect(Collectors.toList())).orElseGet(() -> questionRepository.findByCourseId(courseId));

        Collections.shuffle(filteredQuestions, new Random());
        List<Question> selectedQuestions = filteredQuestions.stream()
                .limit(count)
                .toList();

        return selectedQuestions;
    }

    @Transactional
    public void deleteQuestion(Long questionId, long userId) {
        if (!questionRepository.existsById(questionId)) {
            throw new ResourceNotFoundException("Question not found with ID: " + questionId);
        }

        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + questionId));

        Course course = q.getCourse();

        // not the instructor of the course
        if (course.getInstructor().getId() != userId) {
            throw new InvalidUser("You are not the instructor of this course");
        }

        Question question = questionRepository.findById(questionId).get();

        questionRepository.deleteById(questionId);

        notificationService.notifyUser(question.getCourse().getInstructor().getId(), "Question Deleted Successfully for course " + question.getCourse().getTitle() );
    }

    public QuestionResponseDTO mapToResponseDTO(Question question) {
        QuestionResponseDTO dto = new QuestionResponseDTO();
        dto.setQuestionId(question.getQuestionId());
        dto.setContent(question.getQuestionContent());
        dto.setType(question.getType());
        dto.setAnswer(question.getAnswer());
        dto.setCourseTitle(question.getCourse().getTitle());
        return dto;
    }
}
