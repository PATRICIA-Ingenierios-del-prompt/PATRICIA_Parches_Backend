package ingprompt.patricia.parches.infrastructure.persistence.repository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the Redis invite adapter against a real Redis via
 * Testcontainers. Wires a StringRedisTemplate directly (no Spring context) so
 * the test stays focused. Requires Docker — executed by Failsafe in CI.
 */
@Testcontainers
class RedisInviteRepositoryAdapterIT {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    private static LettuceConnectionFactory connectionFactory;
    private RedisInviteRepositoryAdapter adapter;

    @BeforeAll
    static void startTemplate() {
        connectionFactory = new LettuceConnectionFactory(redis.getHost(), redis.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
    }

    @AfterAll
    static void stopTemplate() {
        connectionFactory.destroy();
    }

    private StringRedisTemplate template() {
        StringRedisTemplate template = new StringRedisTemplate(connectionFactory);
        template.afterPropertiesSet();
        return template;
    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        adapter = new RedisInviteRepositoryAdapter(template());
    }

    @Test
    void saveAndFind_roundTripsParcheId() {
        UUID parcheId = UUID.randomUUID();
        adapter.saveInvite("TOK-EN01", parcheId, 300);

        Optional<UUID> found = adapter.findParcheIdByToken("TOK-EN01");

        assertThat(found).contains(parcheId);
    }

    @Test
    void findByToken_whenAbsent_returnsEmpty() {
        assertThat(adapter.findParcheIdByToken("MISSING")).isEmpty();
    }

    @Test
    void deleteInvite_removesToken() {
        UUID parcheId = UUID.randomUUID();
        adapter.saveInvite("TOK-EN02", parcheId, 300);

        adapter.deleteInvite("TOK-EN02");

        assertThat(adapter.findParcheIdByToken("TOK-EN02")).isEmpty();
    }
}
