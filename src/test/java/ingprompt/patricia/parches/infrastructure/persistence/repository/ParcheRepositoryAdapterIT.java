package ingprompt.patricia.parches.infrastructure.persistence.repository;

import ingprompt.patricia.parches.domain.enums.ParcheCategory;
import ingprompt.patricia.parches.domain.enums.Visibility;
import ingprompt.patricia.parches.domain.model.Parche;
import ingprompt.patricia.parches.infrastructure.persistence.postgre.ParcheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the Postgres persistence adapter and the JPA filter
 * queries, run against a real PostgreSQL via Testcontainers (Flyway builds the
 * schema). Requires Docker — executed by Failsafe in CI.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ParcheRepositoryAdapterIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ParcheRepository repository;

    private ParcheRepositoryAdapter adapter;
    private final Pageable firstPage = PageRequest.of(0, 20);

    @BeforeEach
    void setUp() {
        adapter = new ParcheRepositoryAdapter(repository);
        repository.deleteAll();
    }

    private Parche newParche(String name, ParcheCategory category, Visibility visibility, int maxCapacity, int extraMembers) {
        UUID ownerId = UUID.randomUUID();
        Parche parche = new Parche(UUID.randomUUID(), name, category, maxCapacity, ownerId, "desc", visibility);
        for (int i = 0; i < extraMembers; i++) {
            parche.addMember(UUID.randomUUID());
        }
        return parche;
    }

    @Test
    void saveAndFindById_roundTrips() {
        Parche parche = newParche("Salsa", ParcheCategory.MUSIC, Visibility.PUBLIC, 10, 1);
        adapter.save(parche);

        Parche loaded = adapter.findById(parche.getParcheId()).orElseThrow();
        assertThat(loaded.getName()).isEqualTo("Salsa");
        assertThat(loaded.getMembers()).hasSize(2); // owner + 1
        assertThat(loaded.getVersion()).isNotNull();
    }

    @Test
    void delete_removesRow() {
        Parche parche = newParche("Gone", ParcheCategory.ART, Visibility.PUBLIC, 10, 0);
        adapter.save(parche);

        adapter.delete(parche);

        assertThat(adapter.findById(parche.getParcheId())).isEmpty();
    }

    @Test
    void findByCategory_returnsOnlyMatching() {
        adapter.save(newParche("Music one", ParcheCategory.MUSIC, Visibility.PUBLIC, 10, 0));
        adapter.save(newParche("Art one", ParcheCategory.ART, Visibility.PUBLIC, 10, 0));

        Page<Parche> result = adapter.findByCategory(ParcheCategory.MUSIC, firstPage);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategory()).isEqualTo(ParcheCategory.MUSIC);
    }

    @Test
    void findByVisibility_returnsOnlyMatching() {
        adapter.save(newParche("Public", ParcheCategory.MUSIC, Visibility.PUBLIC, 10, 0));
        adapter.save(newParche("Private", ParcheCategory.MUSIC, Visibility.PRIVATE, 10, 0));

        Page<Parche> result = adapter.findByVisibility(Visibility.PRIVATE, firstPage);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getVisibility()).isEqualTo(Visibility.PRIVATE);
    }

    @Test
    void findByNameContaining_isCaseInsensitive() {
        adapter.save(newParche("Salsa Night", ParcheCategory.MUSIC, Visibility.PUBLIC, 10, 0));
        adapter.save(newParche("Rock show", ParcheCategory.MUSIC, Visibility.PUBLIC, 10, 0));

        Page<Parche> result = adapter.findByNameContaining("salsa", firstPage);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Salsa Night");
    }

    @Test
    void findWithOpenSpots_excludesFullParches() {
        // capacity 2, owner + 1 extra = full
        adapter.save(newParche("Full", ParcheCategory.MUSIC, Visibility.PUBLIC, 2, 1));
        // capacity 5, owner only = open
        adapter.save(newParche("Open", ParcheCategory.MUSIC, Visibility.PUBLIC, 5, 0));

        Page<Parche> result = adapter.findWithOpenSpots(firstPage);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Open");
    }

    @Test
    void findByMember_returnsOnlyParchesTheUserBelongsTo() {
        UUID user = UUID.randomUUID();
        // The user is the owner (owners are members) of one private parche...
        adapter.save(new Parche(UUID.randomUUID(), "Owned", ParcheCategory.MUSIC, 10, user, "desc", Visibility.PRIVATE));
        // ...and an added member of another.
        Parche joined = new Parche(UUID.randomUUID(), "Joined", ParcheCategory.ART, 10, UUID.randomUUID(), "desc", Visibility.PRIVATE);
        joined.addMember(user);
        adapter.save(joined);
        // A parche the user has nothing to do with.
        adapter.save(newParche("Other", ParcheCategory.MUSIC, Visibility.PUBLIC, 10, 0));

        Page<Parche> result = adapter.findByMember(user, firstPage);

        assertThat(result.getContent()).extracting(Parche::getName).containsExactlyInAnyOrder("Owned", "Joined");
    }
}
