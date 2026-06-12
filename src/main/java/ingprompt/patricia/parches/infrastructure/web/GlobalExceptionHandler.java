package ingprompt.patricia.parches.infrastructure.web;

import ingprompt.patricia.parches.domain.exception.CannotRemoveOwnerException;
import ingprompt.patricia.parches.domain.exception.InvalidInviteTokenException;
import ingprompt.patricia.parches.domain.exception.MemberNotFoundInParche;
import ingprompt.patricia.parches.domain.exception.NotParcheOwnerException;
import ingprompt.patricia.parches.domain.exception.ParcheFullException;
import ingprompt.patricia.parches.domain.exception.ParcheIsPrivateException;
import ingprompt.patricia.parches.domain.exception.ParcheIsPublicException;
import ingprompt.patricia.parches.domain.exception.ParcheNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ParcheNotFoundException.class, MemberNotFoundInParche.class})
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CannotRemoveOwnerException.class)
    public ResponseEntity<Map<String, String>> handleCannotRemoveOwner(CannotRemoveOwnerException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(NotParcheOwnerException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(NotParcheOwnerException ex) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(InvalidInviteTokenException.class)
    public ResponseEntity<Map<String, String>> handleInvalidToken(InvalidInviteTokenException ex) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler({ParcheIsPublicException.class, ParcheIsPrivateException.class})
    public ResponseEntity<Map<String, String>> handleVisibilityMismatch(RuntimeException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ParcheFullException.class)
    public ResponseEntity<Map<String, String>> handleFull(ParcheFullException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}
