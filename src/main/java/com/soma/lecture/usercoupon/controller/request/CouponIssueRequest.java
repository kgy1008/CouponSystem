package com.soma.lecture.usercoupon.controller.request;

import jakarta.validation.constraints.NotBlank;

public record CouponIssueRequest(
        @NotBlank(message = "쿠폰 타입이 선택되지 않았습니다.")
        String type
) {
}
