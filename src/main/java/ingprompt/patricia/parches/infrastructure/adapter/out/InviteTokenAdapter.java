package ingprompt.patricia.parches.infrastructure.adapter.out;

import ingprompt.patricia.parches.application.port.out.InviteTokenGeneratorPort;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class InviteTokenAdapter implements InviteTokenGeneratorPort {
    private static final String CHARACTERS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int TOKEN_LENGTH = 8;
    private static final int GROUP_SIZE = 4;        // dash separator every N chars
    private static final char SEPARATOR = '-';
    private static final SecureRandom random = new SecureRandom();

    @Override
    public String generateToken() {
        StringBuilder token = new StringBuilder(TOKEN_LENGTH + (TOKEN_LENGTH / GROUP_SIZE) - 1);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
            if ((i + 1) % GROUP_SIZE == 0 && i < TOKEN_LENGTH - 1) {
                token.append(SEPARATOR);
            }
        }
        return token.toString();
    }
}
