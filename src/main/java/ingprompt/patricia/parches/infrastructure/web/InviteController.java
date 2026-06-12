package ingprompt.patricia.parches.infrastructure.web;

import ingprompt.patricia.parches.application.port.in.InviteUserCase;
import ingprompt.patricia.parches.infrastructure.web.dto.request.CreateInviteRequest;
import ingprompt.patricia.parches.infrastructure.web.dto.response.InviteTokenResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/invites")
@AllArgsConstructor
public class InviteController {
    private final InviteUserCase inviteUserCase;

    @PostMapping
    public ResponseEntity<InviteTokenResponse> invite(@RequestBody CreateInviteRequest request, @RequestHeader("X-User-Id") UUID requesterId) {
        String token = inviteUserCase.inviteUser(request.getParcheId(), requesterId);
        return ResponseEntity.ok(new InviteTokenResponse(token, InviteUserCase.INVITE_TTL_SECONDS));
    }

    @PostMapping("/accept")
    public ResponseEntity<Void> accept(@RequestParam String token, @RequestHeader("X-User-Id") UUID acceptingUserId) {
        inviteUserCase.acceptInvite(token, acceptingUserId);
        return ResponseEntity.noContent().build();
    }
}
