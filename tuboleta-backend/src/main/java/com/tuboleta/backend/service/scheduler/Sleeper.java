package com.tuboleta.backend.service.scheduler;

/**
 * Abstracción de la pausa entre peticiones al mismo proveedor (rate-limit,
 * REQ-DET-005), para poder testear el dispatcher sin esperas reales.
 */
public interface Sleeper {

    /**
     * Pausa el hilo actual por {@code millis} milisegundos.
     *
     * @param millis milisegundos a dormir
     * @throws InterruptedException si el hilo es interrumpido durante la espera
     */
    void sleep(long millis) throws InterruptedException;
}
