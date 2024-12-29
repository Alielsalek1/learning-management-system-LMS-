package com.main.lms.services;

import com.main.lms.dtos.*;
import com.main.lms.entities.*;
import com.main.lms.enums.UserRole;
import com.main.lms.exceptions.InvalidQuizGenerationException;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.exceptions.QuizAlreadySubmittedException;
import com.main.lms.exceptions.ResourceNotFoundException;
import com.main.lms.repositories.*;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final StudentQuizRepository studentQuizRepository;
    private final QuestionService questionService;
    private final EnrolledCourseRepository enrolledCourseRepository;
    private final CourseRepository courseRepository;

    public Quiz getQuizById(Long quizId, User user)  {
        Quiz q = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + quizId));

        Course crs = q.getCourse();

        if (!crs.getInstructor().getId().equals(user.getId()) &&
                enrolledCourseRepository.findByStudentAndCourse(user, crs).isEmpty()) {
            throw new InvalidUser("You are not authorized");
        }

        return q;
    }

    public Quiz generateQuizForCourse(GenerateQuizRequestDTO request, User user) {

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));


        if (!course.getInstructor().getId().equals(user.getId()) &&
                enrolledCourseRepository.findByStudentAndCourse(user, course).isEmpty()) {
            throw new InvalidUser("You are not authorized");
        }

        List<Question> questions = questionRepository.findByCourseId(request.getCourseId());

        if (questions.size() < request.getQuestionCount()) {
            throw new InvalidQuizGenerationException("Not enough questions in the question bank.");
        }
        List<Question> selectedQuestions = questionService.getRandomQuestions(request.getCourseId(), Optional.empty(), request.getQuestionCount());

        Quiz quiz = new Quiz();
        quiz.setCourse(selectedQuestions.get(0).getCourse());
        quiz.setQuestions(selectedQuestions);
        quizRepository.save(quiz);

        return quiz;
    }

    public List<GradeDTO> getStudentQuizGrades(Long studentId) {
        List<StudentQuiz> studentQuizzes = studentQuizRepository.findByStudentId(studentId);
        if (studentQuizzes.isEmpty()) {
            throw new ResourceNotFoundException("No quizzes found for student with ID: " + studentId);
        }

        return studentQuizzes.stream().map(studentQuiz -> {
            GradeDTO dto = new GradeDTO();
            dto.setStudentId(studentQuiz.getStudent().getId());
            dto.setQuizId(studentQuiz.getQuiz().getQuizId());
            dto.setGrade(studentQuiz.getGrade());
            dto.setMaxGrade(studentQuiz.getQuiz().getQuestions().size());
            return dto;
        }).collect(Collectors.toList());
    }

    public GradeDTO submitQuiz(Long quizId, User student, List<QuestionAnswerDTO> answers) throws ResourceNotFoundException {
        Quiz quiz = getQuizById(quizId, student);
        if (studentQuizRepository.existsStudentQuizByStudentIdAndQuiz(student.getId(), quiz)) {
            throw new QuizAlreadySubmittedException("Quiz already submitted by this student.");
        }

        List<Question> questions = quiz.getQuestions();
        int score = 0;
        for (QuestionAnswerDTO answerDTO : answers) {
            Question question = questions.stream().filter(q -> q.getQuestionId().equals(answerDTO.getQuestionId())).findFirst().orElseThrow(
                    () -> new ResourceNotFoundException("Question not found with ID: " + answerDTO.getQuestionId()));
            if (question != null && question.getAnswer().equals(answerDTO.getAnswer())) {
                score++;
            }
        }
        StudentQuiz studentQuiz = new StudentQuiz();
        studentQuiz.setQuiz(quiz);
        studentQuiz.setStudent(student);
        studentQuiz.setGrade((double) score);
        studentQuizRepository.save(studentQuiz);
        return mapToResponseDTO(studentQuiz);
    }

    public List<Quiz> getQuizzesForCourse(Long courseId, User user) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        // Check if the user is an admin or the instructor of the course
        if (!user.getRole().equals(UserRole.ADMIN) && !course.getInstructor().getId().equals(user.getId()) &&
                enrolledCourseRepository.findByStudentAndCourse(user, course).isEmpty()) {
            throw new InvalidUser("You are not authorized to view quizzes for this course");
        }

        // Retrieve all quizzes associated with the course
        List<Quiz> quizzes = quizRepository.findByCourseId(courseId);

        // Map each Quiz entity to QuizDTO
        return quizzes;
    }

    public List<GradeDTO> getQuizGrades(Long quizId, User user) {
        Quiz quiz = getQuizById(quizId, user);

        Course course = quiz.getCourse();

        if (!course.getInstructor().getId().equals(user.getId()) &&
                enrolledCourseRepository.findByStudentAndCourse(user, course).isEmpty()) {
            throw new InvalidUser("You are not authorized");
        }

        List<StudentQuiz> studentQuizzes = studentQuizRepository.findByQuiz(quiz);
        return studentQuizzes.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    private QuizQuestionDTO mapToResponseDTO(Question question) {
        QuizQuestionDTO dto = new QuizQuestionDTO();
        dto.setQuestionId(question.getQuestionId());
        dto.setContent(question.getQuestionContent());
        dto.setType(question.getType());
        return dto;
    }
    public GradeDTO mapToResponseDTO(StudentQuiz studentQuiz) {
        GradeDTO dto = new GradeDTO();
        dto.setStudentId(studentQuiz.getStudent().getId());
        dto.setQuizId(studentQuiz.getQuiz().getQuizId());
        dto.setGrade(studentQuiz.getGrade());
        dto.setMaxGrade(studentQuiz.getQuiz().getQuestions().size());
        return dto;
    }

    public QuizDTO mapToResponseDTO(Quiz quiz) {
        QuizDTO dto = new QuizDTO();
        dto.setQuizId(quiz.getQuizId());
        dto.setCourseId(quiz.getCourse().getId());
        dto.setQuestions(quiz.getQuestions().stream().map(this::mapToResponseDTO).collect(Collectors.toList()));
        return dto;
    }
}
