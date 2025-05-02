package com.soma.lecture.usercoupon.controller;

import com.soma.lecture.common.response.ApiResponse;
import com.soma.lecture.common.response.SuccessCode;
import com.soma.lecture.usercoupon.controller.request.CouponIssueRequest;
import com.soma.lecture.usercoupon.service.UserCouponService;
import com.soma.lecture.usercoupon.service.response.CouponIssueResponse;
import com.soma.lecture.usercoupon.service.response.CouponReadResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PostMapping("/{userUuid}")
    public ApiResponse<CouponIssueResponse> issue(@PathVariable UUID userUuid, @RequestBody @Valid CouponIssueRequest request) {
        CouponIssueResponse response = userCouponService.issue(userUuid, request);
        return ApiResponse.success(SuccessCode.COUPON_ISSUED, response);
    }

    @GetMapping("/{userUuid}")
    public ApiResponse<CouponReadResponse> readMyCoupon(@PathVariable UUID userUuid) {
        CouponReadResponse response = userCouponService.read(userUuid);
        return ApiResponse.success(SuccessCode.COUPON_READ, response);
    }
}
