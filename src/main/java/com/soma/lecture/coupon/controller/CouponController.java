package com.soma.lecture.coupon.controller;

import com.soma.lecture.common.response.ApiResponse;
import com.soma.lecture.common.response.SuccessCode;
import com.soma.lecture.coupon.controller.request.CouponCreateRequest;
import com.soma.lecture.coupon.facade.CouponFacade;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final CouponFacade couponFacade;

    @PostMapping("/{userUuid}")
    public ApiResponse<Void> create(@PathVariable UUID userUuid, @RequestBody @Valid CouponCreateRequest request) {
        couponFacade.createCoupons(userUuid, request);
        return ApiResponse.success(SuccessCode.COUPON_CREATED);
    }
}
