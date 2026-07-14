package ingprompt.patricia.parches.application.service;

import ingprompt.patricia.parches.application.port.in.InviteUserCase;
import ingprompt.patricia.parches.application.port.out.InviteRepositoryOutPort;
import ingprompt.patricia.parches.application.port.out.InviteTokenGeneratorPort;
import ingprompt.patricia.parches.application.port.out.ParcheEventPublisherOut;
import ingprompt.patricia.parches.application.port.out.ParcheRepositoryOutPort;
import ingprompt.patricia.parches.application.service.concurrency.OptimisticRetryExecutor;
import ingprompt.patricia.parches.domain.enums.ParcheCategory;
import ingprompt.patricia.parches.domain.enums.Visibility;
import ingprompt.patricia.parches.domain.exception.InvalidInviteTokenException;
import ingprompt.patricia.parches.domain.exception.NotParcheOwnerException;
import ingprompt.patricia.parches.domain.exception.ParcheFullException;
import ingprompt.patricia.parches.domain.exception.ParcheIsPublicException;
import ingprompt.patricia.parches.domain.model.Parche;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InviteServiceTest {

    @Mock
    private InviteRepositoryOutPort inviteRepository;
    @Mock
    private InviteTokenGeneratorPort tokenGenerator;
    @Mock
    private ParcheRepositoryOutPort parcheRepository;
    @Mock
    private ParcheEventPublisherOut eventPublisher;
    @Mock
    private OptimisticRetryExecutor retryExecutor;

    @InjectMocks
    private InviteService service;

    private UUID ownerId;
    private UUID parcheId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        parcheId = UUID.randomUUID();
    }

    private Parche newParche(Visibility visibility, int maxCapacity) {
        return new Parche(parcheId, "Study group", ParcheCategory.STUDY, maxCapacity, ownerId, "desc", visibility);
    }

    private void runActionInline() {
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(1)).run();
            return null;
        }).when(retryExecutor).runRetrying(any(), any());
    }

    // ---- inviteUser ----

    @Test
    void inviteUser_byOwnerOnPrivateParche_returnsToken() {
        Parche parche = newParche(Visibility.PRIVATE, 10);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        when(tokenGenerator.generateToken()).thenReturn("ABCD-EFGH");

        String token = service.inviteUser(parcheId, ownerId);

        assertThat(token).isEqualTo("ABCD-EFGH");
        verify(inviteRepository).saveInvite(eq("ABCD-EFGH"), eq(parcheId), anyLong());
    }

    @Test
    void inviteUser_byNonOwner_throws() {
        Parche parche = newParche(Visibility.PRIVATE, 10);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));

        assertThatThrownBy(() -> service.inviteUser(parcheId, UUID.randomUUID()))
                .isInstanceOf(NotParcheOwnerException.class);
        verify(inviteRepository, never()).saveInvite(any(), any(), anyLong());
    }

    @Test
    void inviteUser_onPublicParche_throws() {
        Parche parche = newParche(Visibility.PUBLIC, 10);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));

        assertThatThrownBy(() -> service.inviteUser(parcheId, ownerId))
                .isInstanceOf(ParcheIsPublicException.class);
    }

    @Test
    void inviteUser_whenFull_throws() {
        Parche parche = newParche(Visibility.PRIVATE, 1); // owner already fills it
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));

        assertThatThrownBy(() -> service.inviteUser(parcheId, ownerId))
                .isInstanceOf(ParcheFullException.class);
    }

    // ---- acceptInvite ----

    @Test
    void acceptInvite_validToken_addsMemberAndPublishes() {
        Parche parche = newParche(Visibility.PRIVATE, 10);
        UUID joiner = UUID.randomUUID();
        when(inviteRepository.findParcheIdByToken("ABCD-EFGH")).thenReturn(Optional.of(parcheId));
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        runActionInline();

        service.acceptInvite("ABCD-EFGH", joiner);

        assertThat(parche.hasMember(joiner)).isTrue();
        verify(parcheRepository).save(parche);
        verify(eventPublisher).publishNewParcheMember(parcheId, joiner, ParcheCategory.STUDY);
    }

    @Test
    void acceptInvite_invalidToken_throws() {
        when(inviteRepository.findParcheIdByToken("BAD")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.acceptInvite("BAD", UUID.randomUUID()))
                .isInstanceOf(InvalidInviteTokenException.class);
    }

    @Test
    void acceptInvite_whenFull_throws() {
        Parche parche = newParche(Visibility.PRIVATE, 1); // owner already fills it
        when(inviteRepository.findParcheIdByToken("ABCD-EFGH")).thenReturn(Optional.of(parcheId));
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        runActionInline();

        assertThatThrownBy(() -> service.acceptInvite("ABCD-EFGH", UUID.randomUUID()))
                .isInstanceOf(ParcheFullException.class);
    }
}
