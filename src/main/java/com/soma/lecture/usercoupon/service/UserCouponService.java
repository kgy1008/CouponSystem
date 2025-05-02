package com.soma.lecture.usercoupon.service;

import com.soma.lecture.common.exception.BadRequestException;
import com.soma.lecture.common.exception.ConflictException;
import com.soma.lecture.common.exception.NotFoundException;
import com.soma.lecture.common.exception.UnauthorizedException;
import com.soma.lecture.common.response.ErrorCode;
import com.soma.lecture.coupon.domain.Coupon;
import com.soma.lecture.coupon.domain.Type;
import com.soma.lecture.coupon.repository.CouponRepository;
import com.soma.lecture.coupon.service.CouponCountService;
import com.soma.lecture.usercoupon.controller.request.CouponIssueRequest;
import com.soma.lecture.usercoupon.domain.UserCoupon;
import com.soma.lecture.usercoupon.repository.UserCouponRepository;
import com.soma.lecture.usercoupon.service.response.CouponIssueResponse;
import com.soma.lecture.usercoupon.service.response.CouponReadResponse;
import com.soma.lecture.users.domain.Member;
import com.soma.lecture.users.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.List;
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
    public CouponIssueResponse issue(final UUID uuid, final CouponIssueRequest request) {
        Member member = findMemberByUuid(uuid);
        validateUser(member);

        Type type = Type.from(request.type());
        checkCouponCount(type);

        UUID couponUuid = assignCouponToMember(type, member);
        return new CouponIssueResponse(couponUuid, type);
    }

    @Transactional(readOnly = true)
    public CouponReadResponse read(final UUID uuid) {
        Member member = findMemberByUuid(uuid);
        List<UserCoupon> coupons = userCouponRepository.findByUserAndIsUsed(member, false);
        return CouponReadResponse.from(coupons);
    }

    @Transactional
    public void use(final UUID userUuid, final UUID couponUuid) {
        Member member = findMemberByUuid(userUuid);
        Coupon coupon = findCouponByUuid(couponUuid);
        UserCoupon userCoupon = validateUserCoupon(member, coupon);
        userCoupon.useCoupon();
    }

    private UserCoupon validateUserCoupon(final Member member, final Coupon coupon) {
        UserCoupon userCoupon = findUserCoupon(member, coupon);
        if (userCoupon.isUsed()) {
            throw new BadRequestException(ErrorCode.COUPON_ALREADY_USED);
        }

        if (coupon.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException(ErrorCode.COUPON_EXPIRED);
        }
        return userCoupon;
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

    private Member findMemberByUuid(final UUID uuid) {
        return memberRepository.findByUserUuid(uuid)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOTFOUND));
    }

    private Coupon findCouponByUuid(final UUID uuid) {
        return couponRepository.findByCouponUuid(uuid)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COUPON_NOTFOUND));
    }

    private UserCoupon findUserCoupon(final Member member, final Coupon coupon) {
        return userCouponRepository.findByUserAndCoupon(member, coupon)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.UNAUTHORIZED_COUPON_ACCESS));
    }
}
