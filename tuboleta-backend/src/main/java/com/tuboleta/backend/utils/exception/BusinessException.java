package com.tasret.utils.exception;

import lombok.Getter;

/**
 * Excepción personalizada para manejar errores de negocio.
 *
 * <p>Permite asociar una clave de mensaje y argumentos que
 * serán utilizados para obtener el texto traducido desde
 * los archivos de internacionalización (i18n).</p>
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * Clave del mensaje en el archivo de propiedades i18n.
     */
    private final String messageKey;

    /**
     * Argumentos opcionales para reemplazar en el mensaje.
     */
    private final transient Object[] args;

    /**
     * Constructor que inicializa la excepción con clave y argumentos.
     *
     * @param messageKey clave del mensaje de error
     * @param args       valores opcionales para el mensaje
     */
    public BusinessException(String messageKey, Object... args) {
        this.messageKey = messageKey;
        this.args = args;
    }
}