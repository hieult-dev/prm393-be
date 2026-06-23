package com.myfschool.controller;

import com.myfschool.dto.response.ApiResponse;
import com.myfschool.entity.Schedule;
import com.myfschool.service.ScheduleService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController extends AbstractCrudController<Schedule> {

    private final ScheduleService service;

    public ScheduleController(ScheduleService service) {
        super(service);
        this.service = service;
    }

    @GetMapping("/search")
    public ApiResponse<List<Schedule>> search(
            @RequestParam Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate studyDate
    ) {
        if (studyDate == null) {
            return ApiResponse.success(service.findByUserId(userId));
        }
        return ApiResponse.success(service.findByUserIdAndStudyDate(userId, studyDate));
    }
}
