package com.soma.lecture.usercoupon.service;

import com.soma.lecture.common.exception.BadRequestException;
import com.soma.lecture.common.exception.ConflictException;
import com.soma.lecture.common.exception.NotFoundException;
import com.soma.lecture.common.exception.UnauthorizedException;
import com.soma.lecture.common.response.ErrorCode;
import com.soma.lecture.coupon.domain.Coupon;
import com.soma.lecture.coupon.domain.Type;
import com.soma.lecture.coupon.repository.CouponRepository;
import com.soma.lecture.usercoupon.domain.UserCoupon;
import com.soma.lecture.usercoupon.repository.UserCouponRepository;
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

    private static final String COUPON_QUEUE = "coupon_queue:";
    private static final String ISSUED_USER = "issued_users";

    private final UserCouponRepository userCouponRepository;
    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional(readOnly = true)
    public Member validateUser(final UUID uuid) {
        Member member = findMemberByUuid(uuid);
        Boolean isIssued = redisTemplate.opsForSet().isMember(ISSUED_USER, uuid.toString());
        if (Boolean.TRUE.equals(isIssued)) {
            throw new ConflictException(ErrorCode.ALREADY_ISSUED);
        }
        return member;
    }

    @Transactional
    public UserCoupon assignCouponToMember(final Type type, final Member member) {
        UUID couponUuid = popCouponUuidFromRedis(type);
        Coupon coupon = findCouponByUuid(couponUuid);
        UserCoupon userCoupon = new UserCoupon(coupon, member);
        userCouponRepository.save(userCoupon);
        // Redis에 발급 받은 UserUUID 저장
        redisTemplate.opsForSet().add(ISSUED_USER, member.getUserUuid().toString());
        return userCoupon;
    }

    @Transactional(readOnly = true)
    public CouponReadResponse getMyUnusedCoupons(final UUID uuid) {
        Member member = findMemberByUuid(uuid);
        List<UserCoupon> coupons = userCouponRepository.findByUserAndIsUsed(member, false);
        return CouponReadResponse.from(coupons);
    }

    @Transactional
    public void useCoupon(final UUID userUuid, final UUID couponUuid) {
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

    private UserCoupon findUserCoupon(final Member member, final Coupon coupon) {
        return userCouponRepository.findByUserAndCoupon(member, coupon)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.UNAUTHORIZED_COUPON_ACCESS));
    }
}
