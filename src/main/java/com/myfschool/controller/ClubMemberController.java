package com.myfschool.controller;

import com.myfschool.dto.response.ApiResponse;
import com.myfschool.entity.ClubMember;
import com.myfschool.service.ClubMemberService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/club-members")
public class ClubMemberController extends AbstractCrudController<ClubMember> {

    private final ClubMemberService service;

    public ClubMemberController(ClubMemberService service) {
        super(service);
        this.service = service;
    }

    @GetMapping("/search")
    public ApiResponse<List<ClubMember>> search(
            @RequestParam(required = false) Long clubId,
            @RequestParam(required = false) Long userId
    ) {
        if (clubId != null) {
            return ApiResponse.success(service.findByClubId(clubId));
        }
        if (userId != null) {
            return ApiResponse.success(service.findByUserId(userId));
        }
        return ApiResponse.success(service.findAll());
    }
}
