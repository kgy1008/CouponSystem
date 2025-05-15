

package com.soma.lecture.coupon.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.soma.lecture.common.exception.NotFoundException;
import com.soma.lecture.common.exception.UnauthorizedException;
import com.soma.lecture.common.response.ErrorCode;
import com.soma.lecture.coupon.domain.Coupon;
import com.soma.lecture.coupon.domain.Type;
import com.soma.lecture.coupon.repository.CouponRepository;
import com.soma.lecture.users.domain.Member;
import com.soma.lecture.users.domain.Role;
import com.soma.lecture.users.repository.MemberRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class CouponServiceTest {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CouponService couponService;

    @AfterEach
    void cleanup() {
        couponRepository.deleteAll();
    }

    @Test
    @DisplayName("권한이 없는 사용자(MEMBER)가 쿠폰 생성을 시도하면 예외가 발생한다")
    void createCoupons_unauthorized() {
        // given
        Member member = new Member("test@email.com", "password", Role.MEMBER);
        memberRepository.save(member);

        UUID userUuid = member.getUserUuid();

        // when & then
        assertThatThrownBy(() -> couponService.createCoupons(userUuid, Type.CHICKEN, 10))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining(ErrorCode.UNAUTHORIZED_MEMBER.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 쿠폰 생성을 시도하면 예외가 발생한다")
    void createCoupons_memberNotFound() {
        // given
        UUID nonExistentUuid = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> couponService.createCoupons(nonExistentUuid, Type.CHICKEN, 10))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(ErrorCode.MEMBER_NOTFOUND.getMessage());
    }

    @Test
    @DisplayName("관리자가 새로운 타입의 쿠폰을 생성하면 성공한다")
    void createCoupons_newType_success() {
        // given
        Member admin = new Member("admin@email.com", "password", Role.ADMIN);
        memberRepository.save(admin);

        UUID adminUuid = admin.getUserUuid();

        // when
        int remainCount = couponService.createCoupons(adminUuid, Type.CHICKEN, 10);

        // then
        assertThat(remainCount).isEqualTo(10);
        Optional<Coupon> coupon = couponRepository.findByType(Type.CHICKEN);
        assertThat(coupon).isPresent();
        assertThat(coupon.get().getRemainCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("관리자가 이미 존재하는 타입의 쿠폰 수량을 증가시키면 성공한다")
    void createCoupons_existingType_success() {
        // given
        Member admin = new Member("admin@email.com", "password", Role.ADMIN);
        memberRepository.save(admin);

        UUID adminUuid = admin.getUserUuid();

        Coupon existingCoupon = new Coupon(Type.CHICKEN, 5);
        couponRepository.save(existingCoupon);

        // when
        int remainCount = couponService.createCoupons(adminUuid, Type.CHICKEN, 10);

        // then
        assertThat(remainCount).isEqualTo(15);
        Optional<Coupon> coupon = couponRepository.findByType(Type.CHICKEN);
        assertThat(coupon).isPresent();
        assertThat(coupon.get().getRemainCount()).isEqualTo(15);
    }
}

