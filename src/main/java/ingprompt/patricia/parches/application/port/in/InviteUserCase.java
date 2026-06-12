package ingprompt.patricia.parches.application.port.in;

import java.util.UUID;

public interface InviteUserCase {
    String inviteUser(UUID parcheId, UUID userId);
    void acceptInvite(String inviteToken, UUID acceptingUserId);
}
