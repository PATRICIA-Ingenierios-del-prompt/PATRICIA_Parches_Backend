package ingprompt.patricia.parches.domain.exception;

/**
 * 400: el reporte no cumple las reglas de dominio (creador o reportado no
 * pertenecen al parche, auto-reporte, etc.).
 */
public class InvalidReportException extends RuntimeException {
    public InvalidReportException(String reason) {
        super(reason);
    }
}
