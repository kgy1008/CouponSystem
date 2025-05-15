package com.soma.lecture.usercoupon.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RedisLockServiceTest {

    private final RedisLockService redisLockService = new RedisLockService();

    private final String testKey = "test:" + UUID.randomUUID();

    @AfterEach
    void tearDown() {
        redisLockService.unlock(testKey);
    }

    @Test
    @DisplayName("락 획득에 성공한다")
    void lock_success() {
        // when
        boolean result = redisLockService.lock(testKey);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("이미 획득된 락은 다시 획득할 수 없다")
    void lock_alreadyAcquired_returnsFalse() {
        // given
        redisLockService.lock(testKey);

        // when
        boolean result = redisLockService.lock(testKey);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("락을 해제한 후에는 다시 획득할 수 있다")
    void unlock_thenLockAgain_success() {
        // given
        redisLockService.lock(testKey);

        // when
        redisLockService.unlock(testKey);
        boolean result = redisLockService.lock(testKey);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("여러 스레드에서 동시에 락 획득을 시도할 때 하나의 스레드만 성공한다")
    void lock_multipleThreads_onlyOneSucceeds() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    if (redisLockService.lock(testKey)) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 스레드가 작업을 완료할 때까지 기다림
        latch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("다른 키에 대한 락은 독립적으로 획득할 수 있다")
    void lock_differentKeys_bothSucceed() {
        // given
        String anotherKey = "test:" + UUID.randomUUID();

        // when
        boolean result1 = redisLockService.lock(testKey);
        boolean result2 = redisLockService.lock(anotherKey);

        // then
        assertThat(result1).isTrue();
        assertThat(result2).isTrue();

        // clean up
        redisLockService.unlock(anotherKey);
    }
}
