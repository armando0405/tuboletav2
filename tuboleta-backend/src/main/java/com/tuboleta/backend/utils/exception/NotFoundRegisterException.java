package com.tuboleta.backend.utils.exception;

import com.tuboleta.backend.utils.constants.ErrorMessage;
import lombok.Getter;

/**
 * Excepción personalizada para manejar errores de registros inexistentes.
 *
 * <p>Permite asociar argumentos que
 * serán utilizados para obtener el texto traducido desde
 * los archivos de internacionalización (i18n).</p>
 */
@Getter
public class NotFoundRegisterException extends RuntimeException {

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
     * @param args valores opcionales para el mensaje
     */
    public NotFoundRegisterException(Object... args) {
        this.messageKey = ErrorMessage.NOT_FOUND_REGISTER;
        this.args = args;
    }
}