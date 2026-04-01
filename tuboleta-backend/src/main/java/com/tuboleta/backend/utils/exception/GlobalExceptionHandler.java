package com.tuboleta.backend.utils.exception;

import com.tuboleta.backend.utils.constants.ErrorCode;
import com.tuboleta.backend.utils.constants.ErrorMessage;
import com.tuboleta.backend.utils.response.ObjectResponse;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.text.MessageFormat;

/**
 * Manejador global de excepciones para los servicios REST.
 *
 * <p>Centraliza el manejo de errores comunes y retorna respuestas
 * estandarizadas con código, mensaje traducido y estado HTTP.</p>
 */
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Logger propio
     */
    private static final Logger log = LogManager.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja excepciones de negocio personalizadas.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ObjectResponse<Void>> handleBusiness(BusinessException ex) {
        log.error("BusinessException", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ObjectResponse<Void>(ErrorCode.ERROR,
                        MessageFormat.format(ex.getMessageKey(), ex.getArgs()))
        );
    }

    /**
     * Maneja excepciones generales.
     */
    @ExceptionHandler(GenericException.class)
    public ResponseEntity<ObjectResponse<Void>> handleGeneric(GenericException ex) {
        log.error("GenericException", ex);
        return ResponseEntity.status(ex.getStatus()).body(
                new ObjectResponse<Void>(ErrorCode.ERROR,
                        MessageFormat.format(ex.getMessageKey(), ex.getArgs()))
        );
    }

    /**
     * Maneja excepciones de negocio personalizadas.
     */
    @ExceptionHandler(NotFoundRegisterException.class)
    public ResponseEntity<ObjectResponse<Void>> handleNotFoundRegister(NotFoundRegisterException ex) {
        log.error("NotFoundRegisterException", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ObjectResponse<Void>(ErrorCode.ERROR,
                        MessageFormat.format(ex.getMessageKey(), ex.getArgs()))
        );
    }

    /**
     * Maneja errores de validación de argumentos en peticiones.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ObjectResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        log.error("MethodArgumentNotValidException", ex);
        FieldError firstError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message;
        if (firstError != null) {
            String fieldName = firstError.getField();

            Object[] errorArgs = firstError.getArguments();
            Object[] args;

            if (errorArgs != null && errorArgs.length > 1) {
                args = new Object[]{fieldName, errorArgs[1]};
            } else {
                args = new Object[]{fieldName};
            }

            message = MessageFormat.format(firstError.getDefaultMessage(), args);
        } else {
            message = ErrorMessage.VALIDATION;
        }

        return ResponseEntity.badRequest().body(
                new ObjectResponse<Void>(
                        ErrorCode.ERROR,
                        message
                )
        );
    }

    /**
     * Maneja errores por parámetros requeridos ausentes en la petición.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ObjectResponse<Void>> handleMissingRequestParam(
            MissingServletRequestParameterException ex
    ) {
        log.error("MissingServletRequestParameterException", ex);
        return ResponseEntity.badRequest().body(
                new ObjectResponse<Void>(
                        ErrorCode.ERROR,
                        MessageFormat.format(
                                ErrorMessage.MISSING_PARAMETER,
                                ex.getParameterName()
                        )
                )
        );
    }

    /**
     * Maneja excepciones cuando un recurso no es encontrado.
     */
    @ExceptionHandler(ClassNotFoundException.class)
    public ResponseEntity<ObjectResponse<Void>> handleNotFound(ClassNotFoundException ex) {
        log.error("ClassNotFoundException", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ObjectResponse<Void>(ErrorCode.ERROR,
                        ex.getMessage())
        );
    }

    /**
     * Maneja errores por métodos HTTP no soportados.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ObjectResponse<Void>> handleMethodNotAllowed() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
                new ObjectResponse<Void>(ErrorCode.ERROR,
                        ErrorMessage.METHOD_NOT_ALLOWED)
        );
    }

    /**
     * Maneja errores por rutas no encontradas.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ObjectResponse<Void>> handleNotFound(NoHandlerFoundException ex) {
        log.error("NoHandlerFoundException", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ObjectResponse<Void>(
                        ErrorCode.ERROR,
                        ErrorMessage.NOT_FOUND
                )
        );
    }

    /**
     * Maneja cualquier excepción genérica no contemplada.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ObjectResponse<Void>> handleGeneriError(Exception ex) {
        log.error("Exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ObjectResponse<Void>(ErrorCode.ERROR,
                        ErrorMessage.ERROR)
        );
    }
}
