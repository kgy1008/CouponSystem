package com.soma.lecture.usercoupon.service;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.stereotype.Service;

@Service
public class RedisLockService {

    private static final String REDIS_URI = "redis://localhost:6379";
    private static final String REDIS_LOCK_KEY = "LOCKED";
    private static final String SUCCESS_LOCK = "OK";

    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private final RedisCommands<String, String> syncCommands;

    public RedisLockService() {
        RedisURI redisUri = RedisURI.create(REDIS_URI);
        redisClient = RedisClient.create(redisUri);
        connection = redisClient.connect();
        syncCommands = connection.sync();
    }

    public boolean lock(String key) {
        String result = syncCommands.set(key, REDIS_LOCK_KEY, SetArgs.Builder.nx().ex(10));
        return SUCCESS_LOCK.equals(result);
    }

    public void unlock(String key) {
        syncCommands.del(key);
    }
}
