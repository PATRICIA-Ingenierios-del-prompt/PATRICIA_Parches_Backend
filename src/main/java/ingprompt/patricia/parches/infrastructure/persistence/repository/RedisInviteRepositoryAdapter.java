package ingprompt.patricia.parches.infrastructure.persistence.repository;

import ingprompt.patricia.parches.application.port.out.InviteRepositoryOutPort;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
public class RedisInviteRepositoryAdapter implements InviteRepositoryOutPort {
    private static final String KEY_PREFIX = "invite:";
    private final StringRedisTemplate redisTemplate;

    @Override
    public void saveInvite(String token, UUID parcheId, long ttlSeconds) {
        redisTemplate.opsForValue().set(buildKey(token), parcheId.toString(), Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public Optional<UUID> findParcheIdByToken(String token) {
        String value = redisTemplate.opsForValue().get(buildKey(token));
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(value));
    }

    @Override
    public void deleteInvite(String token) {
        redisTemplate.delete(buildKey(token));
    }

    private String buildKey(String token) {
        return KEY_PREFIX + token;
    }
}
