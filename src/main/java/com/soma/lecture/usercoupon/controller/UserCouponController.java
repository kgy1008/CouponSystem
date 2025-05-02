package com.soma.lecture.usercoupon.controller;

import com.soma.lecture.common.response.ApiResponse;
import com.soma.lecture.common.response.SuccessCode;
import com.soma.lecture.usercoupon.controller.request.CouponIssueRequest;
import com.soma.lecture.usercoupon.service.UserCouponService;
import com.soma.lecture.usercoupon.service.response.CouponIssueResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-coupons")
public class UserCouponController {

    private final UserCouponService userCouponService;

    @PostMapping("/{userUUID}")
    public ApiResponse<CouponIssueResponse> issue(@PathVariable String userUUID, @RequestBody @Valid CouponIssueRequest request) {
        CouponIssueResponse response = userCouponService.issue(userUUID, request);
        return ApiResponse.success(SuccessCode.COUPON_ISSUED, response);
    }
}
