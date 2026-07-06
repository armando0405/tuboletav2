package com.tuboleta.backend.utils.constants;

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

    // ===== Task 7: API REST + seguridad (REQ-USU-001/002, REQ-BUS-002/005, REQ-NOT-001) =====

    /**
     * Registro con un correo ya existente.
     */
    public static final String EMAIL_ALREADY_REGISTERED = "El correo {0} ya esta registrado";
    /**
     * Login fallido (credenciales inválidas o usuario inactivo); mensaje
     * deliberadamente genérico para no filtrar el motivo exacto.
     */
    public static final String INVALID_CREDENTIALS = "Credenciales invalidas";
    /**
     * Sin sesión autenticada (AuthenticationEntryPoint).
     */
    public static final String AUTH_REQUIRED = "Debe iniciar sesion para acceder a este recurso";
    /**
     * Autenticado pero sin permisos (AccessDeniedHandler).
     */
    public static final String ACCESS_DENIED = "No tiene permisos para acceder a este recurso";
    /**
     * Canal de notificación (ej. EMAIL) inactivo a nivel global.
     */
    public static final String CHANNEL_INACTIVE = "El canal de notificacion {0} no esta activo";
    /**
     * Canal de notificación inexistente en el catálogo (config inconsistente).
     */
    public static final String CHANNEL_NOT_FOUND = "El canal de notificacion {0} no existe";
    /**
     * Destino duplicado (mismo canal + dirección) para el usuario.
     */
    public static final String DESTINATION_DUPLICATE = "El destino {0} ya esta registrado para este usuario";
    /**
     * Búsqueda duplicada tras normalizar el término (REQ-BUS-002).
     */
    public static final String SEARCH_DUPLICATE = "Ya existe una busqueda equivalente al termino {0}";
    /**
     * Frecuencia de monitoreo fuera de los presets cerrados (REQ-BUS-005).
     */
    public static final String INVALID_FREQUENCY = "La frecuencia de monitoreo debe ser 6, 12, 24 o 48 horas";

}
