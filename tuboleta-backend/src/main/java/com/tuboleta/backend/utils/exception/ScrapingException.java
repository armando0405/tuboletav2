package com.tuboleta.backend.utils.exception;

/**
 * Excepción técnica para errores durante la extracción (scraping) de un
 * proveedor: fallos de red, timeout, HTML de página completa inesperado, etc.
 *
 * <p>A diferencia de {@link BusinessException} y {@link GenericException},
 * no usa clave + argumentos de i18n: transporta un mensaje descriptivo del
 * error técnico junto con su causa original.</p>
 */
public class ScrapingException extends RuntimeException {

    /**
     * Construye la excepción con un mensaje descriptivo y la causa original.
     *
     * @param message descripción del error (p.ej. la URL que falló)
     * @param cause   causa original (p.ej. {@link java.io.IOException} de Jsoup)
     */
    public ScrapingException(String message, Throwable cause) {
        super(message, cause);
    }
}
