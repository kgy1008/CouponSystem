package com.soma.lecture.usercoupon.repository;

import com.soma.lecture.coupon.domain.Coupon;
import com.soma.lecture.usercoupon.domain.UserCoupon;
import com.soma.lecture.users.domain.Member;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    List<UserCoupon> findByUserAndIsUsed(Member user, boolean isUsed);

    Optional<UserCoupon> findByUserAndCoupon(Member member, Coupon coupon);

    Optional<UserCoupon> findByCouponUuid(UUID uuid);
}
