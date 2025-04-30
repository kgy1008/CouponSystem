package com.soma.lecture.coupon.domain;

import com.soma.lecture.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    private static final int COUPON_VALIDITY_DAYS = 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private UUID couponUuid = UUID.randomUUID();

    public Coupon(Type type) {
        this.type = type;
        this.expiredAt = LocalDateTime.now().plusDays(COUPON_VALIDITY_DAYS);
    }
}
