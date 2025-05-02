package com.soma.lecture.coupon.domain.repository;

import com.soma.lecture.coupon.domain.Coupon;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCouponUuid(UUID uuid);
}
