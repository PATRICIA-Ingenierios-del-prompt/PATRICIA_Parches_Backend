package ingprompt.patricia.parches.application.service;

import ingprompt.patricia.parches.application.port.out.ParcheEventPublisherOut;
import ingprompt.patricia.parches.application.port.out.ParcheRepositoryOutPort;
import ingprompt.patricia.parches.application.service.concurrency.OptimisticRetryExecutor;
import ingprompt.patricia.parches.domain.enums.ParcheCategory;
import ingprompt.patricia.parches.domain.enums.ParcheStatus;
import ingprompt.patricia.parches.domain.enums.Visibility;
import ingprompt.patricia.parches.domain.exception.CannotRemoveOwnerException;
import ingprompt.patricia.parches.domain.exception.MemberNotFoundInParche;
import ingprompt.patricia.parches.domain.exception.NotParcheOwnerException;
import ingprompt.patricia.parches.domain.exception.ParcheFullException;
import ingprompt.patricia.parches.domain.exception.ParcheIsPrivateException;
import ingprompt.patricia.parches.domain.exception.ParcheNotFoundException;
import ingprompt.patricia.parches.domain.model.Parche;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParcheServiceTest {

    @Mock
    private ParcheRepositoryOutPort parcheRepository;
    @Mock
    private ParcheEventPublisherOut eventPublisher;
    @Mock
    private OptimisticRetryExecutor retryExecutor;

    @InjectMocks
    private ParcheService service;

    private UUID ownerId;
    private UUID parcheId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        parcheId = UUID.randomUUID();
    }

    private Parche newParche(Visibility visibility, int maxCapacity) {
        return new Parche(parcheId, "Salsa night", ParcheCategory.MUSIC, maxCapacity, ownerId, "desc", visibility);
    }

    // Makes the retry executor run the wrapped action inline (no real transaction).
    private void runActionInline() {
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(1)).run();
            return null;
        }).when(retryExecutor).runRetrying(any(), any());
    }

    // ---- ManageParcheCase ----

    @Test
    void createParche_savesAndPublishes() {
        Parche result = service.createParche("Salsa night", ParcheCategory.MUSIC, 10, ownerId, "desc", Visibility.PUBLIC, "pic");

        assertThat(result.getOwnerId()).isEqualTo(ownerId);
        assertThat(result.getMembers()).contains(ownerId);
        assertThat(result.getPictureUrl()).isEqualTo("pic");
        verify(parcheRepository).save(any(Parche.class));
        verify(eventPublisher).publishParcheWasCreated(eq(result.getParcheId()), eq(ownerId), eq(Visibility.PUBLIC));
    }

    @Test
    void deleteParche_byOwner_deletesAndPublishes() {
        Parche parche = newParche(Visibility.PUBLIC, 10);
        parche.addEvent(UUID.randomUUID());
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));

        service.deleteParche(parcheId, ownerId);

        verify(parcheRepository).delete(parche);
        verify(eventPublisher).publishParcheDeleted(eq(parcheId), eq(ownerId), any());
    }

    @Test
    void deleteParche_byNonOwner_throws() {
        Parche parche = newParche(Visibility.PUBLIC, 10);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));

        assertThatThrownBy(() -> service.deleteParche(parcheId, UUID.randomUUID()))
                .isInstanceOf(NotParcheOwnerException.class);
        verify(parcheRepository, never()).delete(any());
    }

    // ---- ManageMemberParcheCase ----

    @Test
    void joinPublicParche_addsMemberAndPublishes() {
        Parche parche = newParche(Visibility.PUBLIC, 10);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        runActionInline();
        UUID joiner = UUID.randomUUID();

        service.joinPublicParche(parcheId, joiner);

        assertThat(parche.hasMember(joiner)).isTrue();
        verify(parcheRepository).save(parche);
        verify(eventPublisher).publishNewParcheMember(parcheId, joiner);
    }

    @Test
    void joinPublicParche_whenPrivate_throws() {
        Parche parche = newParche(Visibility.PRIVATE, 10);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        runActionInline();

        assertThatThrownBy(() -> service.joinPublicParche(parcheId, UUID.randomUUID()))
                .isInstanceOf(ParcheIsPrivateException.class);
        verify(eventPublisher, never()).publishNewParcheMember(any(), any());
    }

    @Test
    void joinPublicParche_whenFull_throws() {
        Parche parche = newParche(Visibility.PUBLIC, 1); // owner already fills capacity
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        runActionInline();

        assertThatThrownBy(() -> service.joinPublicParche(parcheId, UUID.randomUUID()))
                .isInstanceOf(ParcheFullException.class);
    }

    @Test
    void joinPublicParche_whenAlreadyMember_isNoOp() {
        Parche parche = newParche(Visibility.PUBLIC, 10);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        runActionInline();

        service.joinPublicParche(parcheId, ownerId); // owner is already a member

        verify(parcheRepository, never()).save(any());
        verify(eventPublisher, never()).publishNewParcheMember(any(), any());
    }

    @Test
    void removeMember_byOwner_removesAndPublishes() {
        Parche parche = newParche(Visibility.PUBLIC, 10);
        UUID member = UUID.randomUUID();
        parche.addMember(member);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        runActionInline();

        service.removeMemberFromParche(parcheId, member, ownerId);

        assertThat(parche.hasMember(member)).isFalse();
        verify(parcheRepository).save(parche);
        verify(eventPublisher).publishParcheMemberExpelled(parcheId, member);
    }

    @Test
    void removeMember_byNonOwner_throws() {
        Parche parche = newParche(Visibility.PUBLIC, 10);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        runActionInline();

        assertThatThrownBy(() -> service.removeMemberFromParche(parcheId, UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(NotParcheOwnerException.class);
    }

    @Test
    void removeMember_owner_cannotRemoveItself() {
        Parche parche = newParche(Visibility.PUBLIC, 10);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        runActionInline();

        assertThatThrownBy(() -> service.removeMemberFromParche(parcheId, ownerId, ownerId))
                .isInstanceOf(CannotRemoveOwnerException.class);
    }

    @Test
    void removeMember_whenNotPresent_throws() {
        Parche parche = newParche(Visibility.PUBLIC, 10);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        runActionInline();

        assertThatThrownBy(() -> service.removeMemberFromParche(parcheId, UUID.randomUUID(), ownerId))
                .isInstanceOf(MemberNotFoundInParche.class);
    }

    // ---- ParcheProvisioningCase ----

    @Test
    void assignCommunicationChannels_setsAndSaves() {
        Parche parche = newParche(Visibility.PUBLIC, 10);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        UUID chatId = UUID.randomUUID();
        UUID voiceId = UUID.randomUUID();

        service.assignCommunicationChannels(parcheId, chatId, voiceId);

        assertThat(parche.getCommunication().getChatId()).isEqualTo(chatId);
        verify(parcheRepository).save(parche);
    }

    @Test
    void assignCommunicationChannels_isIdempotent() {
        Parche parche = newParche(Visibility.PUBLIC, 10);
        UUID chatId = UUID.randomUUID();
        UUID voiceId = UUID.randomUUID();
        parche.assignCommunication(chatId, voiceId);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));

        service.assignCommunicationChannels(parcheId, chatId, voiceId);

        verify(parcheRepository, never()).save(any());
    }

    @Test
    void assignParquesTool_setsAndSaves() {
        Parche parche = newParche(Visibility.PUBLIC, 10);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        UUID parquesId = UUID.randomUUID();

        service.assignParquesTool(parcheId, parquesId);

        assertThat(parche.getCollabs().getParquesId()).isEqualTo(parquesId);
        verify(parcheRepository).save(parche);
    }

    @Test
    void assignParquesTool_isIdempotent() {
        Parche parche = newParche(Visibility.PUBLIC, 10);
        UUID parquesId = UUID.randomUUID();
        parche.assignParques(parquesId);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));

        service.assignParquesTool(parcheId, parquesId);

        verify(parcheRepository, never()).save(any());
    }

    @Test
    void assignBoardTool_setsAndSaves() {
        Parche parche = newParche(Visibility.PUBLIC, 10);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        UUID canvasId = UUID.randomUUID();

        service.assignBoardTool(parcheId, canvasId);

        assertThat(parche.getCollabs().getCanvasId()).isEqualTo(canvasId);
        verify(parcheRepository).save(parche);
    }

    @Test
    void assignBoardTool_isIdempotent() {
        Parche parche = newParche(Visibility.PUBLIC, 10);
        UUID canvasId = UUID.randomUUID();
        parche.assignBoard(canvasId);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));

        service.assignBoardTool(parcheId, canvasId);

        verify(parcheRepository, never()).save(any());
    }

    @Test
    void collaboration_splitEvents_assembleToolsAndFlipReady() {
        // Both ids arrive independently (Parques MS + Board MS); together with the
        // communication channels the parche should end up READY.
        Parche parche = newParche(Visibility.PUBLIC, 10);
        UUID parquesId = UUID.randomUUID();
        UUID canvasId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        UUID voiceId = UUID.randomUUID();
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));

        service.assignCommunicationChannels(parcheId, chatId, voiceId);
        service.assignParquesTool(parcheId, parquesId);
        assertThat(parche.getStatus()).isEqualTo(ParcheStatus.PENDING_PROVISIONING);

        service.assignBoardTool(parcheId, canvasId);

        assertThat(parche.getCollabs().getParquesId()).isEqualTo(parquesId);
        assertThat(parche.getCollabs().getCanvasId()).isEqualTo(canvasId);
        assertThat(parche.getStatus()).isEqualTo(ParcheStatus.READY);
    }

    // ---- LinkEventToParcheCase ----

    @Test
    void linkEventToParche_byMember_addsEvent() {
        Parche parche = newParche(Visibility.PUBLIC, 10);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        UUID eventId = UUID.randomUUID();

        service.linkEventToParche(parcheId, eventId, ownerId);

        assertThat(parche.getEvents()).contains(eventId);
        verify(parcheRepository).save(parche);
    }

    @Test
    void linkEventToParche_byNonMember_isDropped() {
        Parche parche = newParche(Visibility.PUBLIC, 10);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));

        service.linkEventToParche(parcheId, UUID.randomUUID(), UUID.randomUUID());

        verify(parcheRepository, never()).save(any());
    }

    @Test
    void linkEventToParche_whenParcheMissing_isDropped() {
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.empty());

        service.linkEventToParche(parcheId, UUID.randomUUID(), ownerId);

        verify(parcheRepository, never()).save(any());
    }

    @Test
    void unlinkEventFromParche_removesEvent() {
        Parche parche = newParche(Visibility.PUBLIC, 10);
        UUID eventId = UUID.randomUUID();
        parche.addEvent(eventId);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));

        service.unlinkEventFromParche(parcheId, eventId);

        assertThat(parche.getEvents()).doesNotContain(eventId);
        verify(parcheRepository).save(parche);
    }

    // ---- ParcheQueryCase ----

    @Test
    void getParcheById_whenFound_returns() {
        Parche parche = newParche(Visibility.PUBLIC, 10);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));

        assertThat(service.getParcheById(parcheId)).isSameAs(parche);
    }

    @Test
    void getParcheById_whenMissing_throws() {
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getParcheById(parcheId))
                .isInstanceOf(ParcheNotFoundException.class);
    }

    @Test
    void getEventsOfParche_returnsEvents() {
        Parche parche = newParche(Visibility.PUBLIC, 10);
        UUID eventId = UUID.randomUUID();
        parche.addEvent(eventId);
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));

        assertThat(service.getEventsOfParche(parcheId)).contains(eventId);
    }

    // ---- SpecialQueriesFilterCases ----

    @Test
    void filterByCategory_delegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Parche> page = new PageImpl<>(List.of(newParche(Visibility.PUBLIC, 10)));
        when(parcheRepository.findByCategory(ParcheCategory.MUSIC, pageable)).thenReturn(page);

        assertThat(service.filterByCategory(ParcheCategory.MUSIC, pageable)).isSameAs(page);
    }

    @Test
    void filterByVisibility_delegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Parche> page = new PageImpl<>(List.of(newParche(Visibility.PUBLIC, 10)));
        when(parcheRepository.findByVisibility(Visibility.PUBLIC, pageable)).thenReturn(page);

        assertThat(service.filterByVisibility(Visibility.PUBLIC, pageable)).isSameAs(page);
    }

    @Test
    void filterByOpenSpots_delegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Parche> page = new PageImpl<>(List.of(newParche(Visibility.PUBLIC, 10)));
        when(parcheRepository.findWithOpenSpots(pageable)).thenReturn(page);

        assertThat(service.filterByOpenSpots(pageable)).isSameAs(page);
    }

    @Test
    void findByName_delegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Parche> page = new PageImpl<>(List.of(newParche(Visibility.PUBLIC, 10)));
        when(parcheRepository.findByNameContaining("salsa", pageable)).thenReturn(page);

        assertThat(service.findByName("salsa", pageable)).isSameAs(page);
    }

    @Test
    void findParchesForMember_delegatesToRepository() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Parche> page = new PageImpl<>(List.of(newParche(Visibility.PRIVATE, 10)));
        when(parcheRepository.findByMember(userId, pageable)).thenReturn(page);

        assertThat(service.findParchesForMember(userId, pageable)).isSameAs(page);
    }
}
