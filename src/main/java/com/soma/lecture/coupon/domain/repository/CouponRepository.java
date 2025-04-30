package com.soma.lecture.coupon.domain.repository;

import com.soma.lecture.coupon.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}
