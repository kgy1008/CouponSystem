package com.soma.lecture.coupon.service;

import com.soma.lecture.coupon.controller.request.CouponCreateRequest;
import com.soma.lecture.coupon.domain.Coupon;
import com.soma.lecture.coupon.domain.CouponCount;
import com.soma.lecture.coupon.domain.Type;
import com.soma.lecture.coupon.domain.repository.CouponCountRepository;
import com.soma.lecture.coupon.domain.repository.CouponRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponCountRepository couponCountRepository;

    public void createCoupons(final CouponCreateRequest request) {
        Type type = Type.from(request.type());
        int count = request.count();
        saveCouponCount(type, count);
        createCoupon(type, count);
    }

    private void saveCouponCount(final Type type, final int count) {
        CouponCount couponCount = new CouponCount(type, count);
        couponCountRepository.save(couponCount);
    }

    private void createCoupon(final Type type, final int count) {
        List<Coupon> coupons = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Coupon coupon = new Coupon(type);
            coupons.add(coupon);
        }
        couponRepository.saveAll(coupons);
    }
}
