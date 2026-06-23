package com.myfschool.controller;

import com.myfschool.dto.response.ApiResponse;
import com.myfschool.entity.Event;
import com.myfschool.service.EventService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventController extends AbstractCrudController<Event> {

    private final EventService service;

    public EventController(EventService service) {
        super(service);
        this.service = service;
    }

    @GetMapping("/search")
    public ApiResponse<List<Event>> search(@RequestParam String status) {
        return ApiResponse.success(service.findByStatus(status));
    }
}
