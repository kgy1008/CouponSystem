package com.soma.lecture.usercoupon.service.response;

import com.soma.lecture.usercoupon.domain.UserCoupon;
import java.util.List;

public record CouponReadResponse(
        List<UserCouponVo> coupons
) {
    public static CouponReadResponse from(final List<UserCoupon> coupons) {
        List<UserCouponVo> result = coupons.stream()
                .map(userCoupon -> new UserCouponVo(userCoupon.getCouponUuid(), userCoupon.getCoupon().getType()))
                .toList();
        return new CouponReadResponse(result);
    }
}
