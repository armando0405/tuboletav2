package com.tasret.utils.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto estándar para respuestas de servicios.
 * Contiene un código, un mensaje y un objeto opcional.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ObjectResponse<T> {

    /**
     * Código de estado (ERROR: -1, SUCCESS: 0).
     */
    @Schema(example = "0")
    private Integer code;

    /**
     * Mensaje descriptivo asociado al código.
     */
    @Schema(example = "El proceso se ejecutó satisfactoriamente")
    private String msg;

    /**
     * Objeto genérico con datos adicionales de la respuesta.
     */
    @Schema(nullable = true)
    private T object;

    /**
     * Constructor simplificado con código y mensaje.
     *
     * @param code código de estado
     * @param msg  mensaje descriptivo
     */
    public ObjectResponse(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
