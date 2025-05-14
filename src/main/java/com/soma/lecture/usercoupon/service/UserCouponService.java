package com.soma.lecture.usercoupon.service;

import com.soma.lecture.common.exception.BadRequestException;
import com.soma.lecture.common.exception.NotFoundException;
import com.soma.lecture.common.exception.UnauthorizedException;
import com.soma.lecture.common.response.ErrorCode;
import com.soma.lecture.coupon.domain.Type;
import com.soma.lecture.coupon.service.CouponCountRedisService;
import com.soma.lecture.usercoupon.domain.UserCoupon;
import com.soma.lecture.usercoupon.repository.UserCouponRepository;
import com.soma.lecture.usercoupon.service.response.CouponReadResponse;
import com.soma.lecture.users.domain.Member;
import com.soma.lecture.users.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCouponService {

    private static final boolean UNUSED = false;

    private final CouponCountRedisService couponCountRedisService;
    private final UserCouponRepository userCouponRepository;
    private final MemberRepository memberRepository;
    private final UserCouponRedisService userCouponRedisService;

    @Transactional(readOnly = true)
    public Member findMemberByUuid(final UUID uuid) {
        return memberRepository.findByUserUuid(uuid)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOTFOUND));
    }

    public void validateUser(final UUID uuid) {
        userCouponRedisService.validateUser(uuid);
    }

    public UUID issueCoupon(final Type type, final Member member) {
        UUID userCouponUUID = createCouponUUID();
        userCouponRedisService.persistCouponIssueToRedis(userCouponUUID, type.name(), member.getUserUuid());
        return userCouponUUID;
    }

    @Transactional
    public void useCoupon(final UUID userUuid, final UUID couponUuid) {
        Member member = findMemberByUuid(userUuid);
        UserCoupon issuedCoupon = findCouponByUuid(couponUuid);
        validateUserCoupon(issuedCoupon, member);
        issuedCoupon.useCoupon();
    }

    @Transactional(readOnly = true)
    public CouponReadResponse getMyUnusedCoupons(final UUID uuid) {
        Member member = findMemberByUuid(uuid);
        List<UserCoupon> coupons = userCouponRepository.findByUserAndIsUsed(member, UNUSED);
        return CouponReadResponse.from(coupons);
    }

    private void validateUserCoupon(final UserCoupon issuedCoupon, final Member member) {
        if (issuedCoupon.isUsed()) { // 이미 사용된 쿠폰
            throw new BadRequestException(ErrorCode.COUPON_ALREADY_USED);
        }

        if (!issuedCoupon.getUser().getUserUuid().equals(member.getUserUuid())) { // 사용자의 쿠폰이 아닐 때
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED_COUPON_ACCESS);
        }

        if (issuedCoupon.getCoupon().getExpired_at().isBefore(LocalDateTime.now())) { // 만료된 쿠폰일 때
            throw new BadRequestException(ErrorCode.COUPON_EXPIRED);
        }
    }

    private UserCoupon findCouponByUuid(final UUID uuid) {
        return userCouponRepository.findByCouponUuid(uuid)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COUPON_NOTFOUND));
    }

    private UUID createCouponUUID() {
        return UUID.randomUUID();
    }
}
