package ingprompt.patricia.parches.application.port.in;

import java.util.UUID;

public interface InviteUserCase {
    long INVITE_TTL_SECONDS = 5L * 60;

    String inviteUser(UUID parcheId, UUID requesterId);
    void acceptInvite(String inviteToken, UUID acceptingUserId);
}
