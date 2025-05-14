package com.soma.lecture.coupon.domain;

import com.soma.lecture.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    private static final int COUPON_VALIDITY_DAYS = 60;

    @Id
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(nullable = false)
    private int remainCount = 0;

    @Column(nullable = false)
    private LocalDateTime expired_at = LocalDateTime.now().plusDays(COUPON_VALIDITY_DAYS);

    public Coupon(final Type type, final int totalCount) {
        this.type = type;
        this.remainCount = totalCount;
    }

    public void increaseCouponCount(final int count) {
        this.remainCount += count;
    }

    public void updateRemainCount(final int count) {
        this.remainCount = count;
    }
}
