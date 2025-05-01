package com.soma.lecture.coupon.service;

import com.soma.lecture.coupon.domain.CouponCount;
import com.soma.lecture.coupon.domain.Type;
import com.soma.lecture.coupon.domain.repository.CouponCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponCountService {

    private final CouponCountRepository couponCountRepository;

    @CachePut(value = "couponCount", key = "#type.name()", cacheManager = "cacheManager")
    public CouponCount saveCouponCount(final Type type, final int count) {
        return couponCountRepository.findByType(type)
                .map(couponCount -> {
                    couponCount.updateRemainCount(count);
                    return couponCount;
                })
                .orElseGet(() -> couponCountRepository.save(new CouponCount(type, count)));
    }
}
