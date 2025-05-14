package com.soma.lecture.coupon.service;

import com.soma.lecture.common.exception.BadRequestException;
import com.soma.lecture.common.response.ErrorCode;
import com.soma.lecture.coupon.domain.Coupon;
import com.soma.lecture.coupon.domain.Type;
import com.soma.lecture.coupon.repository.CouponRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponCountRedisService {

    private static final int MIN_COUPON_COUNT = 0;

    private final CouponRepository couponRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String COUPON_COUNT_KEY = "couponCount:";

    public void cacheCouponCount(final Type type, final int count) {
        redisTemplate.opsForValue().set(getCouponKey(type), String.valueOf(count));
    }

    public int readCouponCount(final Type type) {
        return getCachedCouponCount(type) // cache hit
                .orElseGet(() -> loadFromDatabase(type)); // cache miss
    }

    public void decreaseCouponCount(final Type type) {
        String key = getCouponKey(type);
        Long newCount = redisTemplate.opsForValue().decrement(key);
        if (newCount == null || newCount < MIN_COUPON_COUNT) {
            throw new BadRequestException(ErrorCode.COUPON_SOLD_OUT);
        }
        Coupon coupon = findCoupon(type);
        coupon.updateRemainCount(newCount.intValue());
    }

    private Optional<Integer> getCachedCouponCount(final Type type) {
        String value = redisTemplate.opsForValue().get(getCouponKey(type));
        return Optional.ofNullable(value).map(Integer::parseInt);
    }

    private int loadFromDatabase(final Type type) {
        int count = couponRepository.findByType(type)
                .orElseThrow(() -> new BadRequestException(ErrorCode.INVALID_COUPON_TYPE))
                .getRemainCount();
        redisTemplate.opsForValue().set(getCouponKey(type), String.valueOf(count));
        return count;
    }

    private String getCouponKey(final Type type) {
        return COUPON_COUNT_KEY + type.name();
    }

    private Coupon findCoupon(final Type type) {
        return couponRepository.findByType(type)
                .orElseThrow(() -> new BadRequestException(ErrorCode.INVALID_COUPON_TYPE));
    }
}
