package com.soma.lecture.usercoupon.controller;

import com.soma.lecture.common.response.ApiResponse;
import com.soma.lecture.common.response.SuccessCode;
import com.soma.lecture.usercoupon.controller.request.CouponIssueRequest;
import com.soma.lecture.usercoupon.facade.UserCouponFacade;
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

    private final UserCouponFacade userCouponFacade;

    @PostMapping("/{userUuid}")
    public ApiResponse<CouponIssueResponse> issue(@PathVariable UUID userUuid, @RequestBody @Valid CouponIssueRequest request) {
        CouponIssueResponse response = userCouponFacade.issue(userUuid, request);
        return ApiResponse.success(SuccessCode.COUPON_ISSUED, response);
    }

    @GetMapping("/{userUuid}")
    public ApiResponse<CouponReadResponse> readMyCoupon(@PathVariable UUID userUuid) {
        CouponReadResponse response = userCouponFacade.read(userUuid);
        return ApiResponse.success(SuccessCode.COUPON_READ, response);
    }

    @PostMapping("/{userUuid}/{couponUuid}")
    public ApiResponse<Void> useCoupon(@PathVariable UUID userUuid, @PathVariable UUID couponUuid) {
        userCouponFacade.use(userUuid, couponUuid);
        return ApiResponse.success(SuccessCode.COUPON_USED);
    }
}
