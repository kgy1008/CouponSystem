package com.soma.lecture.coupon.service;

import com.soma.lecture.common.exception.NotFoundException;
import com.soma.lecture.common.exception.UnauthorizedException;
import com.soma.lecture.common.response.ErrorCode;
import com.soma.lecture.coupon.controller.request.CouponCreateRequest;
import com.soma.lecture.coupon.domain.Coupon;
import com.soma.lecture.coupon.domain.CouponCount;
import com.soma.lecture.coupon.domain.Type;
import com.soma.lecture.coupon.domain.repository.CouponCountRepository;
import com.soma.lecture.coupon.domain.repository.CouponRepository;
import com.soma.lecture.users.domain.Member;
import com.soma.lecture.users.domain.Role;
import com.soma.lecture.users.domain.repository.MemberRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponCountRepository couponCountRepository;
    private final MemberRepository memberRepository;

    @Transactional
    @CachePut(value = "couponCount", key = "#request.type()", cacheManager = "cacheManager")
    public CouponCount createCoupons(final String uuid, final CouponCreateRequest request) {
        isUserAuthorized(uuid);
        Type type = Type.from(request.type());
        int count = request.count();
        CouponCount couponCount = saveCouponCount(type, count);
        createCoupon(type, count);
        return couponCount;
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

    public CouponCount saveCouponCount(final Type type, final int count) {
        CouponCount couponCount = new CouponCount(type, count);
        return couponCountRepository.save(couponCount);
    }

    private void createCoupon(final Type type, final int count) {
        List<Coupon> coupons = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Coupon coupon = new Coupon(type);
            coupons.add(coupon);
        }
        couponRepository.saveAll(coupons);
    }
}
