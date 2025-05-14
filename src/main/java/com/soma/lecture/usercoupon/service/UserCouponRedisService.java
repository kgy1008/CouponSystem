package com.soma.lecture.usercoupon.service;

import com.soma.lecture.common.exception.ConflictException;
import com.soma.lecture.common.response.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCouponRedisService {

    private static final String COUPON_QUEUE = "coupon_queue:";
    private static final String ISSUED_USER = "issued_users";

    private final RedisTemplate<String, String> redisTemplate;

    void validateUser(final UUID uuid) {
        Boolean isIssued = redisTemplate.opsForSet().isMember(ISSUED_USER, uuid.toString());
        if (Boolean.TRUE.equals(isIssued)) {
            throw new ConflictException(ErrorCode.ALREADY_ISSUED);
        }
    }

    void persistCouponIssueToRedis(final UUID userCouponUUID, final String type, final UUID userUUID) {
        // Redis Set 자료구조를 활용하여 발급 완료한 UserUUID 저장
        recordUserUUIDToSet(userUUID);
        // Redis List 자료구조를 활용하여 발급 받은 쿠폰 UUID Push
        enqueueIssuedCoupon(userCouponUUID, type);
    }

    UUID pollIssuedCouponUUID(final String type) {
        String couponUUID = redisTemplate.opsForList().leftPop(COUPON_QUEUE + type);
        if (couponUUID == null) {
            return null;
        }
        return UUID.fromString(couponUUID);
    }

    private void recordUserUUIDToSet(final UUID uuid) {
        redisTemplate.opsForSet().add(ISSUED_USER, uuid.toString());
    }

    private void enqueueIssuedCoupon(final UUID uuid, final String type) {
        redisTemplate.opsForList().rightPush(COUPON_QUEUE + type, uuid.toString());
    }
}
