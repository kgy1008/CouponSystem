package com.soma.lecture.coupon.service;

import com.soma.lecture.common.exception.NotFoundException;
import com.soma.lecture.common.exception.UnauthorizedException;
import com.soma.lecture.common.response.ErrorCode;
import com.soma.lecture.coupon.domain.Coupon;
import com.soma.lecture.coupon.domain.Type;
import com.soma.lecture.coupon.repository.CouponRepository;
import com.soma.lecture.users.domain.Member;
import com.soma.lecture.users.domain.Role;
import com.soma.lecture.users.repository.MemberRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public int createCoupons(final UUID uuid, final Type type, final int count) {
        isUserAuthorized(uuid);
        return createCoupon(type, count);
    }

    private void isUserAuthorized(final UUID uuid) {
        Member member = findMember(uuid);
        if (member.getRole() == Role.MEMBER) {
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED_MEMBER);
        }
    }

    private Member findMember(final UUID uuid) {
        return memberRepository.findByUserUuid(uuid)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOTFOUND));
    }

    private int createCoupon(final Type type, final int count) {
        Coupon coupon = couponRepository.findByType(type)
                .map(existing -> {
                    existing.updateRemainCount(count);
                    return existing;
                })
                .orElseGet(() -> new Coupon(type, count));
        couponRepository.save(coupon);
        return coupon.getRemainCount();
    }
}
