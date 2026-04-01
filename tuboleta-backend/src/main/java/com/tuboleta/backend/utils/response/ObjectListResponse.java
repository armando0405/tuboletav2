package com.tasret.utils.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Respuesta estándar para servicios que retornan listas.
 * Incluye código, mensaje y una colección de objetos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ObjectListResponse<T> {

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
     * Lista de objetos devueltos en la respuesta.
     */
    @Schema(nullable = true)
    private List<T> list;

    /**
     * Constructor simplificado con código y mensaje.
     *
     * @param code código de estado
     * @param msg  mensaje descriptivo
     */
    public ObjectListResponse(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
