package com.tuboleta.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Habilita el mecanismo de {@code @Scheduled} (REQ-DET-005) y define el pool
 * fijo usado por {@code MonitoringDispatcher} para el paralelismo ENTRE
 * proveedores.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

    @Bean
    public ThreadPoolTaskExecutor monitoringTaskExecutor(@Value("${scheduler.pool-size:3}") int poolSize) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setThreadNamePrefix("monitor-");
        executor.initialize();
        return executor;
    }
}
