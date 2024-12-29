package com.main.lms.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialRequestDTO {

    private long CourseId;
    private String Material;

    public MaterialRequestDTO() {
    }
}
