package com.soma.lecture.usercoupon.domain.repository;

import com.soma.lecture.coupon.domain.Coupon;
import com.soma.lecture.usercoupon.domain.UserCoupon;
import com.soma.lecture.users.domain.Member;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    @Query("SELECT CASE WHEN EXISTS (SELECT 1 FROM UserCoupon uc WHERE uc.user = :user) THEN true ELSE false END")
    boolean existsByUser(@Param("user") Member user);

    List<UserCoupon> findByUserAndIsUsed(Member user, boolean isUsed);

    Optional<UserCoupon> findByUserAndCoupon(Member member, Coupon coupon);
}
