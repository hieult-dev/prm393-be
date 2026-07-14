package com.myfschool.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AssignStudentsRequest(
        @NotNull @Size(max = 100) List<@NotNull Long> studentIds
) {
}
