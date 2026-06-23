package com.myfschool.controller;

import com.myfschool.dto.response.ApiResponse;
import com.myfschool.entity.Club;
import com.myfschool.service.ClubService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clubs")
public class ClubController extends AbstractCrudController<Club> {

    private final ClubService service;

    public ClubController(ClubService service) {
        super(service);
        this.service = service;
    }

    @GetMapping("/search")
    public ApiResponse<List<Club>> search(@RequestParam String status) {
        return ApiResponse.success(service.findByStatus(status));
    }
}
