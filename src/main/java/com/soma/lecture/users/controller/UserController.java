package com.soma.lecture.users.controller;

import com.soma.lecture.common.response.ApiResponse;
import com.soma.lecture.common.response.SuccessCode;
import com.soma.lecture.users.controller.request.MemberCreateRequest;
import com.soma.lecture.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ApiResponse<Void> create(@RequestBody @Valid MemberCreateRequest request) {
        userService.signUp(request);
        return ApiResponse.success(SuccessCode.MEMBER_CREATED);
    }
}
