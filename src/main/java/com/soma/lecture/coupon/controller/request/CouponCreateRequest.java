package com.soma.lecture.coupon.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CouponCreateRequest(
        @NotBlank(message = "쿠폰 타입을 입력해주세요")
        String type,
        @Min(value = 1, message = "0보다 큰 숫자를 입력해주세요")
        int count
) {
}
