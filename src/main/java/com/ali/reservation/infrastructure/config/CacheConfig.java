package com.ali.reservation.infrastructure.config;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("slots", "users");
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    private CacheProperties.Caffeine<Object, Object> caffeineCacheBuilder() {
        return CacheProperties.Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .recordStats();
    }
}