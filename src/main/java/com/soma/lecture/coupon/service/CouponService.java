package com.soma.lecture.coupon.service;

import com.soma.lecture.coupon.controller.request.CouponCreateRequest;
import com.soma.lecture.coupon.domain.Coupon;
import com.soma.lecture.coupon.domain.CouponCount;
import com.soma.lecture.coupon.domain.Type;
import com.soma.lecture.coupon.domain.repository.CouponCountRepository;
import com.soma.lecture.coupon.domain.repository.CouponRepository;
import com.soma.lecture.coupon.service.response.CouponCreateResponse;
import com.soma.lecture.coupon.service.response.vo.CreatedCoupon;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponCountRepository couponCountRepository;

    public CouponCreateResponse createCoupons(CouponCreateRequest request) {
        Type type = Type.from(request.type());
        int count = request.count();

        CouponCount couponCount = new CouponCount(type, count);
        couponCountRepository.save(couponCount);
        List<CreatedCoupon> createdCoupons = createCoupon(type, count);

        return new CouponCreateResponse(createdCoupons);
    }

    private List<CreatedCoupon> createCoupon(Type type, int count) {
        List<CreatedCoupon> createdCoupons = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Coupon coupon = new Coupon(type);
            couponRepository.save(coupon);
            createdCoupons.add(new CreatedCoupon(coupon.getId(), type));
        }
        return createdCoupons;
    }
}
