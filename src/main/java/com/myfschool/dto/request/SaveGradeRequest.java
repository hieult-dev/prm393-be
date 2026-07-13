package com.myfschool.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SaveGradeRequest(
        @NotNull Long userId,
        @NotNull Long subjectId,
        @NotNull Long semesterId,
        @NotEmpty @Size(max = 20) List<@Valid GradeItemInput> items
) {
}
