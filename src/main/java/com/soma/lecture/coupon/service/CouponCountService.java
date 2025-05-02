package com.soma.lecture.coupon.service;

import com.soma.lecture.common.exception.BadRequestException;
import com.soma.lecture.common.exception.NotFoundException;
import com.soma.lecture.common.response.ErrorCode;
import com.soma.lecture.coupon.domain.CouponCount;
import com.soma.lecture.coupon.domain.Type;
import com.soma.lecture.coupon.repository.CouponCountRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponCountService {

    private final CouponCountRepository couponCountRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String COUPON_COUNT_KEY = "couponCount";

    public void saveCouponCount(final Type type, final int count) {
        CouponCount couponCount = couponCountRepository.findByType(type)
                .map(existing -> {
                    existing.updateRemainCount(count);
                    return existing;
                })
                .orElseGet(() -> new CouponCount(type, count));

        couponCountRepository.save(couponCount);
        redisTemplate.opsForHash().put(COUPON_COUNT_KEY, type.name(), count);
    }

    public int readCouponCount(final Type type) {
        return getCachedCouponCount(type) // cache hit
                .orElseGet(() -> loadFromDbAndCache(type)); // cache miss
    }

    public void decreaseCouponCount(final Type type) {
        CouponCount couponCount = couponCountRepository.findByType(type)
                .orElseThrow(() -> new NotFoundException(ErrorCode.INVALID_COUPON_TYPE));

        couponCount.decrementRemainCount();
        int newCount = couponCount.getRemainCount();
        redisTemplate.opsForHash().put(COUPON_COUNT_KEY, type.name(), newCount);
    }

    private Optional<Integer> getCachedCouponCount(Type type) {
        return Optional.ofNullable((Integer) redisTemplate.opsForHash().get(COUPON_COUNT_KEY, type.name()));
    }

    private int loadFromDbAndCache(Type type) {
        int count = couponCountRepository.findByType(type)
                .orElseThrow(() -> new BadRequestException(ErrorCode.INVALID_COUPON_TYPE))
                .getRemainCount();
        redisTemplate.opsForHash().put(COUPON_COUNT_KEY, type.name(), count);
        return count;
    }
}
