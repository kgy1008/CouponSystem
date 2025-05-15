package com.soma.lecture.usercoupon.service;

import com.soma.lecture.common.exception.NotFoundException;
import com.soma.lecture.common.response.ErrorCode;
import com.soma.lecture.coupon.domain.Coupon;
import com.soma.lecture.coupon.domain.Type;
import com.soma.lecture.coupon.repository.CouponRepository;
import com.soma.lecture.usercoupon.domain.UserCoupon;
import com.soma.lecture.usercoupon.repository.UserCouponRepository;
import com.soma.lecture.users.domain.Member;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponAsyncProcessor {

    private final UserCouponRedisService userCouponRedisService;
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

    @Async
    @Transactional
    public void consumeAndCreateUserCoupon(final Type type, final Member member) {
        UUID couponUUID = userCouponRedisService.pollIssuedCouponUUID(type.name());
        if (couponUUID == null) {
            log.info("반영이 완료되었습니다");
        }
        Coupon coupon = findCouponByType(type);
        UserCoupon userCoupon = new UserCoupon(coupon, member, couponUUID);
        userCouponRepository.save(userCoupon);
    }

    private Coupon findCouponByType(final Type type) {
        return couponRepository.findByType(type)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COUPON_NOTFOUND));
    }
}
