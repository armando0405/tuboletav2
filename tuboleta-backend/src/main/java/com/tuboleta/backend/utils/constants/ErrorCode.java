package com.tasret.utils.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constantes con códigos de respuesta estándar para servicios REST.
 *
 * <p>Estos códigos se utilizan en las respuestas JSON para indicar
 * el resultado de una operación: éxito o error.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorCode {
    /**
     * Código que indica un error en la operación.
     */
    public static final Integer ERROR = -1;
    /**
     * Código que indica éxito en la operación.
     */
    public static final Integer SUCCESS = 0;
}
