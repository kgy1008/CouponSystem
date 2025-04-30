package com.soma.lecture.users.controller;

import com.soma.lecture.common.response.ApiResponse;
import com.soma.lecture.common.response.SuccessCode;
import com.soma.lecture.users.controller.request.MemberRequest;
import com.soma.lecture.users.service.UserService;
import com.soma.lecture.users.service.response.UserLoginResponse;
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
    public ApiResponse<Void> create(@RequestBody @Valid MemberRequest request) {
        userService.signUp(request);
        return ApiResponse.success(SuccessCode.MEMBER_CREATED);
    }

    @PostMapping("/login")
    public ApiResponse<UserLoginResponse> login(@RequestBody @Valid MemberRequest request) {
        UserLoginResponse response = userService.login(request);
        return ApiResponse.success(SuccessCode.MEMBER_AUTHENTICATED, response);
    }
}
