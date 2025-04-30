package com.soma.lecture.coupon.service.response.vo;

import com.soma.lecture.coupon.domain.Type;

public record CreatedCoupon(
        Long id,
        Type type
) {
}
