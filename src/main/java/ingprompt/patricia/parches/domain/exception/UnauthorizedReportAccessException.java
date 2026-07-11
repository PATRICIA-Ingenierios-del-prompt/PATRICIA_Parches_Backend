package ingprompt.patricia.parches.domain.exception;

import java.util.UUID;

/**
 * 403: quien pide leer un reporte no es el dueno del parche ni tiene un rol
 * autorizado (ROLE_ADMIN por ahora).
 */
public class UnauthorizedReportAccessException extends RuntimeException {
    public UnauthorizedReportAccessException(UUID requesterId, String roles) {
        super("Requester " + requesterId + " with roles '" + roles
                + "' is not authorized to read parche reports");
    }
}
