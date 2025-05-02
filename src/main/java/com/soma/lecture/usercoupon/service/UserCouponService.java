package com.soma.lecture.usercoupon.service;

import com.soma.lecture.common.exception.BadRequestException;
import com.soma.lecture.common.exception.ConflictException;
import com.soma.lecture.common.exception.NotFoundException;
import com.soma.lecture.common.response.ErrorCode;
import com.soma.lecture.coupon.domain.Coupon;
import com.soma.lecture.coupon.domain.Type;
import com.soma.lecture.coupon.domain.repository.CouponRepository;
import com.soma.lecture.coupon.service.CouponCountService;
import com.soma.lecture.usercoupon.controller.request.CouponIssueRequest;
import com.soma.lecture.usercoupon.domain.UserCoupon;
import com.soma.lecture.usercoupon.domain.repository.UserCouponRepository;
import com.soma.lecture.usercoupon.service.response.CouponIssueResponse;
import com.soma.lecture.users.domain.Member;
import com.soma.lecture.users.domain.repository.MemberRepository;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCouponService {

    private static final int MIN_COUPON_COUNT = 0;
    private static final String COUPON_QUEUE = "coupon_queue:";

    private final CouponCountService couponCountService;
    private final UserCouponRepository userCouponRepository;
    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public CouponIssueResponse issue(final String userUUID, final @Valid CouponIssueRequest request) {
        UUID uuid = UUID.fromString(userUUID);
        Member member = findMemberByUuid(uuid);
        validateUser(member);
        Type type = Type.from(request.type());
        int couponCount = couponCountService.readCouponCount(type);
        checkCouponCount(couponCount);

        // Redis에서 쿠폰 발급
        String couponQueueKey = COUPON_QUEUE + type.name();
        UUID couponUuid = UUID.fromString(redisTemplate.opsForList().leftPop(couponQueueKey));
        Coupon coupon = findCouponByUuid(couponUuid);
        UserCoupon userCoupon = new UserCoupon(coupon, member);
        userCouponRepository.save(userCoupon);

        couponCountService.decreaseCouponCount(type);
        return new CouponIssueResponse(couponUuid, type);
    }

    private Member findMemberByUuid(final UUID uuid) {
        return memberRepository.findByUserUuid(uuid)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOTFOUND));
    }

    private Coupon findCouponByUuid(final UUID uuid) {
        return couponRepository.findByCouponUuid(uuid)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COUPON_NOTFOUND));
    }

    private void validateUser(final Member member) {
        boolean exists = userCouponRepository.existsByUser(member);
        if (exists) {
            throw new ConflictException(ErrorCode.ALREADY_ISSUED);
        }
    }

    private void checkCouponCount(final int couponCount) {
        if (couponCount <= MIN_COUPON_COUNT) {
            throw new BadRequestException(ErrorCode.COUPON_SOLD_OUT);
        }
    }
}
