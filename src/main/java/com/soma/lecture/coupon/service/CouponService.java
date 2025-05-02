package com.soma.lecture.coupon.service;

import com.soma.lecture.common.exception.NotFoundException;
import com.soma.lecture.common.exception.UnauthorizedException;
import com.soma.lecture.common.response.ErrorCode;
import com.soma.lecture.coupon.controller.request.CouponCreateRequest;
import com.soma.lecture.coupon.domain.Coupon;
import com.soma.lecture.coupon.domain.Type;
import com.soma.lecture.coupon.domain.repository.CouponRepository;
import com.soma.lecture.users.domain.Member;
import com.soma.lecture.users.domain.Role;
import com.soma.lecture.users.domain.repository.MemberRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private static final String COUPON_QUEUE = "coupon_queue:";

    private final CouponCountService couponCountService;
    private final CouponRepository couponRepository;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public void createCoupons(final String uuid, final CouponCreateRequest request) {
        isUserAuthorized(uuid);
        Type type = Type.from(request.type());
        int count = request.count();
        couponCountService.saveCouponCount(type, count);
        createCoupon(type, count);
    }

    private void isUserAuthorized(final String userUUID) {
        UUID uuid = UUID.fromString(userUUID);
        Member member = findMember(uuid);
        if (member.getRole() == Role.MEMBER) {
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED_MEMBER);
        }
    }

    private Member findMember(final UUID uuid) {
        return memberRepository.findByUserUuid(uuid)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOTFOUND));
    }

    private void createCoupon(final Type type, final int count) {
        List<Coupon> coupons = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Coupon coupon = new Coupon(type);
            coupons.add(coupon);
        }
        couponRepository.saveAll(coupons);
        saveCouponInRedis(coupons);
    }

    private void saveCouponInRedis(final List<Coupon> coupons) {
        for (Coupon coupon : coupons) {
            redisTemplate.opsForList().leftPush(COUPON_QUEUE + coupon.getType(), coupon.getCouponUuid().toString());
        }
    }
}
