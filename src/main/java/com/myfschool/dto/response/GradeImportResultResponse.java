package com.myfschool.dto.response;

import java.util.List;

public record GradeImportResultResponse(
        int importedRows,
        int createdRows,
        int updatedRows,
        int skippedRows,
        int failedRows,
        List<String> errors
) {
}
