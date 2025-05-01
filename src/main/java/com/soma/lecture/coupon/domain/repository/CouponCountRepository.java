package com.soma.lecture.coupon.domain.repository;

import com.soma.lecture.coupon.domain.CouponCount;
import com.soma.lecture.coupon.domain.Type;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponCountRepository extends JpaRepository<CouponCount, Long> {

    Optional<CouponCount> findByType(Type type);
}
