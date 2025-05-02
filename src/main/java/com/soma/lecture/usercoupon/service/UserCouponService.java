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
import java.util.Optional;
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
    public CouponIssueResponse issue(final String userUUID, final CouponIssueRequest request) {
        UUID uuid = UUID.fromString(userUUID);
        Member member = findMemberByUuid(uuid);
        validateUser(member);

        Type type = Type.from(request.type());
        checkCouponCount(type);

        UUID couponUuid = assignCouponToMember(type, member);
        return new CouponIssueResponse(couponUuid, type);
    }

    private UUID assignCouponToMember(final Type type, final Member member) {
        UUID couponUuid = popCouponUuidFromRedis(type);
        Coupon coupon = findCouponByUuid(couponUuid);
        userCouponRepository.save(new UserCoupon(coupon, member));
        couponCountService.decreaseCouponCount(type);
        return couponUuid;
    }

    private UUID popCouponUuidFromRedis(final Type type) {
        String couponQueueKey = COUPON_QUEUE + type.name();
        return Optional.ofNullable(redisTemplate.opsForList().leftPop(couponQueueKey))
                .map(UUID::fromString)
                .orElseThrow(() -> new BadRequestException(ErrorCode.COUPON_SOLD_OUT));
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

    private void checkCouponCount(final Type type) {
        int couponCount = couponCountService.readCouponCount(type);
        if (couponCount <= MIN_COUPON_COUNT) {
            throw new BadRequestException(ErrorCode.COUPON_SOLD_OUT);
        }
    }
}
