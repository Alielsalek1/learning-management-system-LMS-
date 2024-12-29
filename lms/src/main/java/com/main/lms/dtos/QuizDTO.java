package com.main.lms.dtos;

import com.main.lms.entities.Question;
import lombok.Data;

import java.util.List;

@Data
public class QuizDTO {
    private Long quizId;
    private Long courseId;
    private List<QuizQuestionDTO> questions;
}
