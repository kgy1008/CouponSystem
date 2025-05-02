package com.soma.lecture.usercoupon.domain;

import com.soma.lecture.common.BaseEntity;
import com.soma.lecture.coupon.domain.Coupon;
import com.soma.lecture.users.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_coupon",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "user_coupon_UN",
                        columnNames = {"user_id", "coupon_id"}
                )
        }
) // 유니크 제약 조건 설정
public class UserCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member user;

    @Column(nullable = false)
    private boolean isUsed = false;

    @Column(nullable = true)
    private LocalDateTime usedAt;

    public UserCoupon(final Coupon coupon, final Member user) {
        this.coupon = coupon;
        this.user = user;
    }
}
