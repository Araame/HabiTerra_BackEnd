package com.habiterra.shared.exception;
import com.habiterra.identity.exception.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(com.habiterra.property.exception.PropertyException.class)
    ResponseEntity<ApiError> property(com.habiterra.property.exception.PropertyException e, HttpServletRequest request) {
        return error(e.getStatus(), e.getCode(), e.getMessage(), request);
    }
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    ResponseEntity<ApiError> uploadTooLarge(Exception e, HttpServletRequest request) {
        return error(413, "PHOTO_TOO_LARGE", "Photo exceeds the upload size limit", request);
    }
    @ExceptionHandler({org.springframework.web.bind.MissingServletRequestParameterException.class,
            org.springframework.web.multipart.support.MissingServletRequestPartException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
            jakarta.validation.ConstraintViolationException.class})
    ResponseEntity<ApiError> invalidInput(Exception e, HttpServletRequest request) {
        return error(400, "INVALID_REQUEST", "Invalid request parameters", request);
    }
    @ExceptionHandler(org.springframework.dao.PessimisticLockingFailureException.class)
    ResponseEntity<ApiError> concurrentUpdate(Exception e, HttpServletRequest request) {
        return error(409, "CONCURRENT_UPDATE", "Concurrent property update; retry the request", request);
    }
    private final ApiErrorWriter errors;
    public GlobalExceptionHandler(ApiErrorWriter errors){this.errors=errors;}
    private ResponseEntity<ApiError> error(int s,String c,String m,HttpServletRequest r){
        return ResponseEntity.status(s).body(errors.body(s,c,m,r.getRequestURI()));
    }
    @ExceptionHandler(AuthException.class)
    ResponseEntity<ApiError> business(AuthException e,HttpServletRequest r){return error(e.getStatus(),e.getCode(),e.getMessage(),r);}
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> authentication(AuthenticationException e,HttpServletRequest r){return ResponseEntity.status(401)
        .header("WWW-Authenticate","Bearer").body(errors.body(401,"BAD_CREDENTIALS","Identifiants incorrects",r.getRequestURI()));}
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> denied(AccessDeniedException e,HttpServletRequest r){return error(403,"ACCESS_DENIED","Acces interdit",r);}
    @ExceptionHandler({MethodArgumentNotValidException.class,HttpMessageNotReadableException.class})
    ResponseEntity<ApiError> validation(Exception e,HttpServletRequest r){return error(400,"INVALID_REQUEST","Champs invalides ou role inconnu",r);}
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> conflict(DataIntegrityViolationException e,HttpServletRequest r){return error(409,"IDENTIFIER_CONFLICT","Email ou telephone deja utilise, ou donnees incompatibles",r);}
    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiError> missing(Exception e,HttpServletRequest r){return error(404,"NOT_FOUND","Ressource introuvable",r);}
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiError> method(Exception e,HttpServletRequest r){return error(405,"METHOD_NOT_ALLOWED","Methode non autorisee",r);}
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception e,HttpServletRequest r){return error(500,"INTERNAL_ERROR","Erreur interne",r);}
}
