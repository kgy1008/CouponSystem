package com.soma.lecture.coupon.controller.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CouponCreateRequest(
        @NotNull(message = "생성할 쿠폰을 입력해주세요")
        List<CouponRequest> coupons
){
}
