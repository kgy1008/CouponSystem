package com.soma.lecture.coupon.controller;

import com.soma.lecture.common.response.ApiResponse;
import com.soma.lecture.common.response.SuccessCode;
import com.soma.lecture.coupon.controller.request.CouponCreateRequest;
import com.soma.lecture.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/admin")
    public ApiResponse<Void> create(@RequestBody @Valid CouponCreateRequest request) {
        couponService.createCoupons(request);
        return ApiResponse.success(SuccessCode.COUPON_CREATED);
    }

}
