package com.jameshao.gp22023237.DTO;

import lombok.Data;
import java.util.List;

@Data
public class BatchSelectionDTO {
    private Long studentId; // 学生id
    private List<ChoiceItem> choices; // 志愿列表

    @Data
    public static class ChoiceItem {
        private Long mentorId; // 导师id
        private Integer studentChoiceOrder; // 志愿顺序
    }
}
