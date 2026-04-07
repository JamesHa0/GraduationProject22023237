package com.jameshao.gp22023237.DTO;

import lombok.Data;
import java.util.List;

@Data
public class BatchCourseSelectionDTO {
    private Long studentId;
    private List<ChoiceItem> choices;

    @Data
    public static class ChoiceItem {
        private Long courseId;
    }
}
