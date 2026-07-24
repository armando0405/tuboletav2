package com.tuboleta.backend.service.scheduler;

import org.springframework.stereotype.Component;

/**
 * Implementación real de {@link Sleeper}, basada en {@link Thread#sleep(long)}.
 */
@Component
public class ThreadSleeper implements Sleeper {

    @Override
    public void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }
}
