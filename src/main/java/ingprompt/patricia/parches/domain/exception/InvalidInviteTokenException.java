package ingprompt.patricia.parches.domain.exception;

public class InvalidInviteTokenException extends RuntimeException {
    public InvalidInviteTokenException() {
        super("Invite token is invalid or has expired");
    }
}
