package ingprompt.patricia.parches.infrastructure.web;

import ingprompt.patricia.parches.application.port.in.ParcheAdminCase;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/parches/admin")
@AllArgsConstructor
public class AdminParcheController {

    private static final Set<String> AUTHORIZED_ROLES = Set.of("ADMIN");

    private final ParcheAdminCase parcheAdminCase;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> stats(@RequestHeader("X-User-Roles") String roles) {
        if (!hasAuthorizedRole(roles)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(Map.of("totalParches", parcheAdminCase.countParches()));
    }

    private boolean hasAuthorizedRole(String roles) {
        return roles != null && java.util.Arrays.stream(roles.split(","))
                .map(String::trim)
                .anyMatch(AUTHORIZED_ROLES::contains);
    }
}
