package com.soma.lecture.coupon.controller;

import com.soma.lecture.coupon.controller.request.CouponCreateRequest;
import com.soma.lecture.coupon.service.CouponService;
import com.soma.lecture.coupon.service.response.CouponCreateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/admin")
    public ResponseEntity<CouponCreateResponse> create(@RequestBody @Valid CouponCreateRequest request) {
        CouponCreateResponse response = couponService.createCoupons(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
