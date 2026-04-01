package com.tasret.utils.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constantes con mensajes de error estándar para servicios REST.
 *
 * <p>Estos mensajes se utilizan en las respuestas JSON para proporcionar
 * información al cliente sobre el resultado de una operación. Los mensajes
 * pueden contener placeholders {0}, {1}, etc. para ser reemplazados
 * dinámicamente.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorMessage {
    /**
     * Mensaje genérico de error.
     */
    public static final String ERROR = "Ha ocurrido un error inesperado";
    /**
     * Mensaje cuando un recurso no es encontrado.
     */
    public static final String NOT_FOUND = "Recurso no encontrado";
    /**
     * Mensaje para errores de lógica de negocio.
     */
    public static final String ERROR_BUSINESS = "Error de negocio: {0}";
    /**
     * Mensaje para error de validación en campo específico.
     */
    public static final String VALIDATION = "Error en el campo {0}";
    /**
     * Mensaje cuando un método HTTP no es permitido.
     */
    public static final String METHOD_NOT_ALLOWED = "Metodo no permitido";
    /**
     * Mensaje de éxito genérico.
     */
    public static final String SUCCESS = "El proceso se ejecuto satisfactoriamente";
    /**
     * Mensaje cuando un registro no es encontrado.
     */
    public static final String NOT_FOUND_REGISTER = "{0} no se encontro para {1}: {2}";
    /**
     * Mensaje cuando falta un parámetro requerido.
     */
    public static final String MISSING_PARAMETER = "El parametro {0} es requerido";
    /**
     * Mensaje cuando el usuario no está autorizado.
     */
    public static final String UNAUTHORIZED = "Usuario no autorizado";
    /**
     * Mensaje que indica que el servicio está funcionando correctamente.
     */
    public static final String SERVICE_OK = "El servicio esta en funcionamiento";

}
