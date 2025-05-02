package com.soma.lecture.usercoupon.service.response;

import com.soma.lecture.coupon.domain.Type;
import java.util.UUID;

public record UserCouponVo(
        UUID couponUuid,
        Type type
) {
}
