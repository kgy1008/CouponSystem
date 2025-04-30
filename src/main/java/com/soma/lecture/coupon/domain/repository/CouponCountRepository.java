package com.soma.lecture.coupon.domain.repository;

import com.soma.lecture.coupon.domain.CouponCount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponCountRepository extends JpaRepository<CouponCount, Long> {
}
