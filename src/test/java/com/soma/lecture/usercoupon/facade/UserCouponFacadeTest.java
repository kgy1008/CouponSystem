package com.soma.lecture.usercoupon.facade;

import static org.assertj.core.api.Assertions.assertThat;

import com.soma.lecture.common.exception.ConflictException;
import com.soma.lecture.coupon.domain.Type;
import com.soma.lecture.coupon.service.CouponCountRedisService;
import com.soma.lecture.usercoupon.controller.request.CouponIssueRequest;
import com.soma.lecture.usercoupon.service.CouponAsyncProcessor;
import com.soma.lecture.usercoupon.service.RedisLockService;
import com.soma.lecture.usercoupon.service.UserCouponService;
import com.soma.lecture.usercoupon.service.response.CouponIssueResponse;
import com.soma.lecture.users.domain.Member;
import com.soma.lecture.users.domain.Role;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class UserCouponFacadeTest {

    @Mock
    private UserCouponService userCouponService;
    @Mock
    private CouponCountRedisService couponCountRedisService;
    @Mock
    private RedisLockService redisLockService;
    @Mock
    private CouponAsyncProcessor couponAsyncProcessor;

    @InjectMocks
    private UserCouponFacade userCouponFacade;

    @Test
    @DisplayName("동일 사용자 동시 요청 시 쿠폰은 한 번만 발급된다.")
    void issue_concurrentLockTest() throws Exception {
        UUID uuid = UUID.randomUUID();
        CouponIssueRequest request = new CouponIssueRequest("CHICKEN");
        Member member = new Member("test@email.com", "pw", Role.MEMBER);

        // 락: 첫 번째만 true, 나머지는 false
        Mockito.when(redisLockService.lock(Mockito.anyString()))
                .thenReturn(true, false, false, false, false);

        Mockito.when(userCouponService.findMemberByUuid(uuid)).thenReturn(member);
        Mockito.doNothing().when(userCouponService).validateUser(uuid);
        Mockito.when(couponCountRedisService.readCouponCount(Type.CHICKEN)).thenReturn(10);
        Mockito.when(userCouponService.issueCoupon(Type.CHICKEN, member)).thenReturn(UUID.randomUUID());
        Mockito.doNothing().when(couponCountRedisService).decreaseCouponCount(Type.CHICKEN);
        Mockito.doNothing().when(couponAsyncProcessor).consumeAndCreateUserCoupon(Type.CHICKEN, member);
        Mockito.doNothing().when(redisLockService).unlock(Mockito.anyString());

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Object>> results = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            results.add(executor.submit(() -> {
                try {
                    return userCouponFacade.issue(uuid, request);
                } catch (ConflictException e) {
                    return e;
                }
            }));
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        long successCount = results.stream().filter(f -> {
            try {
                return f.get() instanceof CouponIssueResponse;
            } catch (Exception e) {
                return false;
            }
        }).count();

        long conflictCount = results.stream().filter(f -> {
            try {
                return f.get() instanceof ConflictException;
            } catch (Exception e) {
                return false;
            }
        }).count();

        assertThat(successCount).isEqualTo(1);
        assertThat(conflictCount).isEqualTo(threadCount - 1);
    }

    @Test
    @DisplayName("여러 사용자가 동시에 요청해도 쿠폰이 1개만 발급되고 나머지는 품절 예외가 발생한다.")
    void issue_concurrentMultiUser_couponSoldOutTest() throws Exception {
        int threadCount = 5;
        CouponIssueRequest request = new CouponIssueRequest("CHICKEN");
        List<UUID> userUuids = new ArrayList<>();
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            UUID uuid = UUID.randomUUID();
            userUuids.add(uuid);
            members.add(new Member("user" + i + "@test.com", "pwdfj#32", Role.MEMBER));
            Mockito.when(userCouponService.findMemberByUuid(uuid)).thenReturn(members.get(i));
            Mockito.doNothing().when(userCouponService).validateUser(uuid);
        }

        // 락은 모두 성공하도록
        Mockito.when(redisLockService.lock(Mockito.anyString())).thenReturn(true);
        // 쿠폰 개수: 첫 번째만 1, 이후 0 반환
        Mockito.when(couponCountRedisService.readCouponCount(Type.CHICKEN))
                .thenReturn(1, 0, 0, 0, 0);
        Mockito.when(userCouponService.issueCoupon(Mockito.eq(Type.CHICKEN), Mockito.any(Member.class)))
                .thenReturn(UUID.randomUUID());
        Mockito.doNothing().when(couponCountRedisService).decreaseCouponCount(Type.CHICKEN);
        Mockito.doNothing().when(couponAsyncProcessor)
                .consumeAndCreateUserCoupon(Mockito.eq(Type.CHICKEN), Mockito.any(Member.class));
        Mockito.doNothing().when(redisLockService).unlock(Mockito.anyString());

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Object>> results = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            results.add(executor.submit(() -> {
                try {
                    return userCouponFacade.issue(userUuids.get(idx), request);
                } catch (Exception e) {
                    return e;
                }
            }));
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        long successCount = results.stream().filter(f -> {
            try {
                return f.get() instanceof CouponIssueResponse;
            } catch (Exception e) {
                return false;
            }
        }).count();

        long soldOutCount = results.stream().filter(f -> {
            try {
                Object result = f.get();
                return result instanceof com.soma.lecture.common.exception.BadRequestException;
            } catch (Exception e) {
                return false;
            }
        }).count();

        assertThat(successCount).isEqualTo(1);
        assertThat(soldOutCount).isEqualTo(threadCount - 1);
    }
}
