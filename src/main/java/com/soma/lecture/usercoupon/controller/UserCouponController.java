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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserCouponController {

    private final UserCouponFacade userCouponFacade;

    @PostMapping("/user-coupons")
    public ApiResponse<CouponIssueResponse> issue(@RequestHeader("X-User-UUID") UUID userUUID,
                                                  @RequestBody @Valid CouponIssueRequest request) {
        CouponIssueResponse response = userCouponFacade.issue(userUUID, request);
        return ApiResponse.success(SuccessCode.COUPON_ISSUED, response);
    }

    @GetMapping("/user-coupons")
    public ApiResponse<CouponReadResponse> readMyCoupon(@RequestHeader("X-User-UUID") UUID userUUID) {
        CouponReadResponse response = userCouponFacade.read(userUUID);
        return ApiResponse.success(SuccessCode.COUPON_READ, response);
    }

    @PostMapping("/user-coupons/{couponUuid}")
    public ApiResponse<Void> useCoupon(@RequestHeader("X-User-UUID") UUID userUUID, @PathVariable UUID couponUuid) {
        userCouponFacade.use(userUUID, couponUuid);
        return ApiResponse.success(SuccessCode.COUPON_USED);
    }
}
