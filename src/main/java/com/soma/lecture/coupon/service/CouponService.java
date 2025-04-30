package com.soma.lecture.coupon.service;

import com.soma.lecture.coupon.controller.request.CouponRequest;
import com.soma.lecture.coupon.domain.Coupon;
import com.soma.lecture.coupon.domain.Type;
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

    public CouponCreateResponse createCoupons(List<CouponRequest> request) {
        List<CreatedCoupon> createdCoupons = new ArrayList<>();
        for (CouponRequest couponRequest : request) {
            Type type = Type.from(couponRequest.type());
            int count = couponRequest.count();

            for (int i=0; i< count; i++) {
                Coupon coupon = new Coupon(count, type);
                couponRepository.save(coupon);
                createdCoupons.add(new CreatedCoupon(coupon.getId(), type));
            }
        }

        return new CouponCreateResponse(createdCoupons);
    }
}
