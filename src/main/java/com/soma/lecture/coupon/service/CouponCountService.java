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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponCountService {

    private static final int MIN_COUPON_COUNT = 0;

    private final CouponCountRepository couponCountRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String COUPON_COUNT_KEY = "couponCount:";

    public void updateCouponCount(final Type type, final int count) {
        CouponCount couponCount = couponCountRepository.findByType(type)
                .map(existing -> {
                    existing.updateRemainCount(count);
                    return existing;
                })
                .orElseGet(() -> new CouponCount(type, count));

        couponCountRepository.save(couponCount);
        redisTemplate.opsForValue().set(getCouponKey(type), String.valueOf(couponCount.getRemainCount()));
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
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void syncCouponCountsToDb() {
        for (Type type : Type.values()) {
            String key = getCouponKey(type);
            String countStr = redisTemplate.opsForValue().get(key);
            if (countStr == null) continue;

            long remainCount = Long.parseLong(countStr);
            CouponCount couponCount = couponCountRepository.findByType(type)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.INVALID_COUPON_TYPE));

            couponCount.decreaseRemainCount((int) remainCount);
        }
    }

    private Optional<Integer> getCachedCouponCount(final Type type) {
        String value = redisTemplate.opsForValue().get(getCouponKey(type));
        return Optional.ofNullable(value).map(Integer::parseInt);
    }

    private int loadFromDatabase(final Type type) {
        int count = couponCountRepository.findByType(type)
                .orElseThrow(() -> new BadRequestException(ErrorCode.INVALID_COUPON_TYPE))
                .getRemainCount();
        redisTemplate.opsForValue().set(getCouponKey(type), String.valueOf(count));
        return count;
    }

    private String getCouponKey(final Type type) {
        return COUPON_COUNT_KEY + type.name();
    }
}
