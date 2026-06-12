package ingprompt.patricia.parches.infrastructure.adapter.out;

import ingprompt.patricia.parches.application.port.out.InviteTokenGeneratorPort;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class InviteTokenAdapter implements InviteTokenGeneratorPort {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int TOKEN_LENGTH = 8;
    private static final SecureRandom random = new SecureRandom();

    @Override
    public String generateToken() {
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return token.toString();
    }
}
