package com.soma.lecture.coupon.service;

import com.soma.lecture.common.exception.BadRequestException;
import com.soma.lecture.common.exception.NotFoundException;
import com.soma.lecture.common.response.ErrorCode;
import com.soma.lecture.coupon.domain.CouponCount;
import com.soma.lecture.coupon.domain.Type;
import com.soma.lecture.coupon.repository.CouponCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponCountService {

    private final CouponCountRepository couponCountRepository;

    // Write-Through 전략
    @CachePut(value = "couponCount", key = "#type.name()", cacheManager = "cacheManager")
    public int saveCouponCount(final Type type, final int count) {
        return couponCountRepository.findByType(type)
                .map(couponCount -> {
                    couponCount.updateRemainCount(count);
                    return couponCount.getRemainCount();
                })
                .orElseGet(() -> {
                    couponCountRepository.save(new CouponCount(type, count));
                    return count;
                });
    }

    @Cacheable(value = "couponCount", key = "#type.name()", cacheManager = "cacheManager")
    public int readCouponCount(final Type type) {
        CouponCount couponCount = couponCountRepository.findByType(type)
                .orElseThrow(() -> new BadRequestException(ErrorCode.INVALID_COUPON_TYPE));

        return couponCount.getRemainCount();
    }

    @CachePut(value = "couponCount", key = "#type.name()", cacheManager = "cacheManager")
    public int decreaseCouponCount(final Type type) {
        return couponCountRepository.findByType(type)
                .map(couponCount -> {
                    couponCount.decrementRemainCount();
                    return couponCount.getRemainCount();
                })
                .orElseThrow(() -> new NotFoundException(ErrorCode.INVALID_COUPON_TYPE));
    }
}
