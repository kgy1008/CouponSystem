package com.soma.lecture.usercoupon.facade;

import com.soma.lecture.common.exception.BadRequestException;
import com.soma.lecture.common.exception.ConflictException;
import com.soma.lecture.common.response.ErrorCode;
import com.soma.lecture.coupon.domain.Type;
import com.soma.lecture.coupon.service.CouponCountService;
import com.soma.lecture.usercoupon.controller.request.CouponIssueRequest;
import com.soma.lecture.usercoupon.domain.UserCoupon;
import com.soma.lecture.usercoupon.service.RedisLockService;
import com.soma.lecture.usercoupon.service.UserCouponService;
import com.soma.lecture.usercoupon.service.response.CouponIssueResponse;
import com.soma.lecture.usercoupon.service.response.CouponReadResponse;
import com.soma.lecture.users.domain.Member;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCouponFacade {

    private static final int MIN_COUPON_COUNT = 0;

    private final UserCouponService userCouponService;
    private final CouponCountService couponCountService;
    private final RedisLockService redisLockService;

    @Transactional
    public CouponIssueResponse issue(final UUID uuid, final CouponIssueRequest request) {
        Type type = Type.from(request.type());
        Member member = userCouponService.validateUser(uuid);

        if (!redisLockService.lock(uuid.toString())) {
            throw new ConflictException(ErrorCode.ALREADY_ISSUED);
        }

        try {
            checkCouponCount(type);
            UserCoupon userCoupon = userCouponService.assignCouponToMember(type, member);
            couponCountService.decreaseCouponCount(type);
            return new CouponIssueResponse(userCoupon.getCoupon().getCouponUuid(), type);
        } finally {
            redisLockService.unlock(uuid.toString());
        }
    }

    @Transactional(readOnly = true)
    public CouponReadResponse read(final UUID uuid) {
        return userCouponService.getMyUnusedCoupons(uuid);
    }

    @Transactional
    public void use(final UUID userUuid, final UUID couponUuid) {
        userCouponService.useCoupon(userUuid, couponUuid);
    }

    private void checkCouponCount(final Type type) {
        int couponCount = couponCountService.readCouponCount(type);
        if (couponCount <= MIN_COUPON_COUNT) {
            throw new BadRequestException(ErrorCode.COUPON_SOLD_OUT);
        }
    }
}
