package com.soma.lecture.coupon.service.response;

import com.soma.lecture.coupon.service.response.vo.CreatedCoupon;
import java.util.List;

public record CouponCreateResponse(
        List<CreatedCoupon> createdCoupons
) {
}
