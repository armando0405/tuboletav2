package com.tasret.utils.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Excepción genericas para manejar errores.
 *
 * <p>Permite asociar un estado, una clave de mensaje y argumentos que
 * serán utilizados para obtener el texto traducido desde
 * los archivos de internacionalización (i18n).</p>
 */
@Getter
public class GenericException extends RuntimeException {

    /**
     * Estado de error.
     */
    private final HttpStatus status;

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
     * @param status     estado del error
     * @param messageKey clave del mensaje de error
     * @param args       valores opcionales para el mensaje
     */
    public GenericException(HttpStatus status, String messageKey, Object... args) {
        this.status = status;
        this.messageKey = messageKey;
        this.args = args;
    }
}