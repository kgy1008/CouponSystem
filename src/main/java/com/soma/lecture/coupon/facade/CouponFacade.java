package com.soma.lecture.coupon.facade;

import com.soma.lecture.coupon.controller.request.CouponCreateRequest;
import com.soma.lecture.coupon.domain.Type;
import com.soma.lecture.coupon.service.CouponCountService;
import com.soma.lecture.coupon.service.CouponService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponFacade {

    private final CouponService couponService;
    private final CouponCountService couponCountService;

    @Transactional
    public void createCoupons(final UUID uuid, final CouponCreateRequest request) {
        Type type = Type.from(request.type());
        int count = request.count();
        couponService.createCoupons(uuid, type, count);
        couponCountService.updateCouponCount(type, count);
    }
}
