package ingprompt.patricia.parches.infrastructure.adapter.out;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class InviteTokenAdapterTest {

    // 8 chars from the human-friendly charset, dash-separated into 4+4.
    private static final Pattern TOKEN = Pattern.compile("^[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}$");

    private final InviteTokenAdapter adapter = new InviteTokenAdapter();

    @Test
    void generateToken_matchesExpectedFormat() {
        String token = adapter.generateToken();
        assertThat(token).matches(TOKEN);
    }

    @Test
    void generateToken_neverUsesAmbiguousCharacters() {
        // No O, 0, I, L or 1 — repeat enough to exercise the charset.
        IntStream.range(0, 500).forEach(i -> {
            String token = adapter.generateToken();
            assertThat(token).matches(TOKEN);
            assertThat(token.replace("-", "")).doesNotContainAnyWhitespaces();
            assertThat(token).doesNotContain("O", "0", "I", "L", "1");
        });
    }

    @Test
    void generateToken_isReasonablyUnique() {
        String a = adapter.generateToken();
        String b = adapter.generateToken();
        // Not a strict guarantee, but a collision here is astronomically unlikely.
        assertThat(a).isNotEqualTo(b);
    }
}
