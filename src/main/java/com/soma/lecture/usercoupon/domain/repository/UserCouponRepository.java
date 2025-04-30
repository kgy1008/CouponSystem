package com.soma.lecture.usercoupon.domain.repository;

import com.soma.lecture.usercoupon.domain.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
}
