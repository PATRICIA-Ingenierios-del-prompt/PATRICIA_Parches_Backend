package ingprompt.patricia.parches.infrastructure.web;

import ingprompt.patricia.parches.domain.exception.NotParcheOwnerException;
import ingprompt.patricia.parches.domain.exception.ParcheNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ParcheNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ParcheNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NotParcheOwnerException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(NotParcheOwnerException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }
}
