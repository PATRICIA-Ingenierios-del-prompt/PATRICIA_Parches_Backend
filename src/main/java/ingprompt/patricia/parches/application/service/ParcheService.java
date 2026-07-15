package ingprompt.patricia.parches.application.service;

import ingprompt.patricia.parches.application.port.in.LinkEventToParcheCase;
import ingprompt.patricia.parches.application.port.in.ManageMemberParcheCase;
import ingprompt.patricia.parches.application.port.in.ManageParcheCase;
import ingprompt.patricia.parches.application.port.in.ParcheProvisioningCase;
import ingprompt.patricia.parches.application.port.in.ParcheQueryCase;
import ingprompt.patricia.parches.application.port.in.SpecialQueriesFilterCases;
import ingprompt.patricia.parches.application.port.out.ParcheEventPublisherOut;
import ingprompt.patricia.parches.application.port.out.ParcheRepositoryOutPort;
import ingprompt.patricia.parches.application.service.concurrency.OptimisticRetryExecutor;
import ingprompt.patricia.parches.domain.enums.ParcheCategory;
import ingprompt.patricia.parches.domain.enums.Visibility;
import ingprompt.patricia.parches.domain.exception.*;
import ingprompt.patricia.parches.domain.model.CollaborationTools;
import ingprompt.patricia.parches.domain.model.CommunicationChannels;
import ingprompt.patricia.parches.domain.model.Parche;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class ParcheService implements ManageParcheCase, ParcheProvisioningCase, ManageMemberParcheCase, LinkEventToParcheCase, ParcheQueryCase, SpecialQueriesFilterCases {
    private final ParcheRepositoryOutPort parcheRepository;
    private final ParcheEventPublisherOut eventPublisher;
    private final OptimisticRetryExecutor retryExecutor;

    @Override
    @Transactional
    public Parche createParche(String name, ParcheCategory category, int num, UUID ownerId, String description, Visibility visibility, String pictureUrl) {
        Parche parche = new Parche(UUID.randomUUID(), name, category, num, ownerId, description, visibility);
        parche.setPictureUrl(pictureUrl);
        parcheRepository.save(parche);
        eventPublisher.publishParcheWasCreated(parche.getParcheId(), parche.getName(), parche.getOwnerId(), parche.getVisibility(), parche.getCategory());
        return parche;
    }

    @Override
    @Transactional
    public void deleteParche(UUID parcheId, UUID ownerId) {
        Parche parche = parcheRepository.findById(parcheId).orElseThrow(() -> new ParcheNotFoundException(parcheId));
        if (!parche.isOwnedBy(ownerId)) {
            throw new NotParcheOwnerException(parcheId, ownerId);
        }
        Set<UUID> eventIdsSnapshot = new HashSet<>(parche.getEvents());

        parcheRepository.delete(parche);
        eventPublisher.publishParcheDeleted(parcheId, ownerId, eventIdsSnapshot);
    }

    @Override
    public void joinPublicParche(UUID parcheId, UUID userId) {
        retryExecutor.runRetrying(parcheId, () -> doJoinPublicParche(parcheId, userId));
    }

    private void doJoinPublicParche(UUID parcheId, UUID userId) {
        Parche parche = parcheRepository.findById(parcheId).orElseThrow(() -> new ParcheNotFoundException(parcheId));
        if (parche.isPrivate()) {
            throw new ParcheIsPrivateException(parcheId);
        }
        if (parche.hasMember(userId)) {
            return;
        }
        if (parche.isFull()) {
            throw new ParcheFullException(parcheId);
        }

        parche.addMember(userId);
        parcheRepository.save(parche);
        eventPublisher.publishNewParcheMember(parcheId, userId, parche.getCategory());
    }

    @Override
    public void removeMemberFromParche(UUID parcheId, UUID memberId, UUID requesterId) {
        retryExecutor.runRetrying(parcheId, () -> doRemoveMember(parcheId, memberId, requesterId));
    }

    private void doRemoveMember(UUID parcheId, UUID memberId, UUID requesterId) {
        Parche parche = parcheRepository.findById(parcheId).orElseThrow(() -> new ParcheNotFoundException(parcheId));
        if (!parche.isOwnedBy(requesterId)) {
            throw new NotParcheOwnerException(parcheId, requesterId);
        }
        if (parche.isOwnedBy(memberId)) {
            throw new CannotRemoveOwnerException(parcheId);
        }
        if (!parche.hasMember(memberId)) {
            throw new MemberNotFoundInParche(parcheId, memberId);
        }

        parche.deleteMember(memberId);
        parcheRepository.save(parche);
        eventPublisher.publishParcheMemberExpelled(parcheId, memberId);
    }

    @Override
    @Transactional
    public void assignCommunicationChannels(UUID parcheId, UUID chatId, UUID voiceId) {
        Parche parche = parcheRepository.findById(parcheId).orElseThrow(() -> new ParcheNotFoundException(parcheId));
        CommunicationChannels current = parche.getCommunication();
        if (current != null && Objects.equals(current.getChatId(), chatId) && Objects.equals(current.getVoiceId(), voiceId)) {
            log.debug("Communication channels already set for parche {}; skipping", parcheId);
            return;
        }

        parche.assignCommunication(chatId, voiceId);
        parcheRepository.save(parche);
    }

    @Override
    @Transactional
    public void assignParquesTool(UUID parcheId, UUID parquesId) {
        Parche parche = parcheRepository.findById(parcheId).orElseThrow(() -> new ParcheNotFoundException(parcheId));
        CollaborationTools current = parche.getCollabs();
        if (current != null && Objects.equals(current.getParquesId(), parquesId)) {
            log.debug("Parques id already set for parche {}; skipping", parcheId);
            return;
        }

        parche.assignParques(parquesId);
        parcheRepository.save(parche);
    }

    @Override
    @Transactional
    public void assignBoardTool(UUID parcheId, UUID canvasId) {
        Parche parche = parcheRepository.findById(parcheId).orElseThrow(() -> new ParcheNotFoundException(parcheId));
        CollaborationTools current = parche.getCollabs();
        if (current != null && Objects.equals(current.getCanvasId(), canvasId)) {
            log.debug("Board (canvas) id already set for parche {}; skipping", parcheId);
            return;
        }

        parche.assignBoard(canvasId);
        parcheRepository.save(parche);
    }

    @Override
    @Transactional
    public void linkEventToParche(UUID parcheId, UUID eventId, UUID userId) {
        Optional<Parche> maybeParche = parcheRepository.findById(parcheId);
        if (maybeParche.isEmpty()) {
            log.warn("Dropping event-link: parche {} not found (event {}, user {})", parcheId, eventId, userId);
            return;
        }
        Parche parche = maybeParche.get();
        if (!parche.hasMember(userId)) {
            log.warn("Dropping event-link: user {} is not a member of parche {} (event {})", userId, parcheId, eventId);
            return;
        }
        parche.addEvent(eventId);
        parcheRepository.save(parche);
    }

    @Override
    @Transactional
    public void unlinkEventFromParche(UUID parcheId, UUID eventId) {
        parcheRepository.findById(parcheId).ifPresent(parche -> {
            parche.removeEvent(eventId);
            parcheRepository.save(parche);
        });
    }

    @Override
    public Parche getParcheById(UUID parcheId) {
        return parcheRepository.findById(parcheId).orElseThrow(() -> new ParcheNotFoundException(parcheId));
    }

    @Override
    public Set<UUID> getEventsOfParche(UUID parcheId) {
        return getParcheById(parcheId).getEvents();
    }

    @Override
    public Set<UUID> getMembersOfParche(UUID parcheId) {
        return getParcheById(parcheId).getMembers();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Parche> filterByCategory(ParcheCategory category, Pageable pageable) {
        return parcheRepository.findByCategory(category, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Parche> filterByVisibility(Visibility visibility, Pageable pageable) {
        return parcheRepository.findByVisibility(visibility, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Parche> filterByOpenSpots(Pageable pageable) {
        return parcheRepository.findWithOpenSpots(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Parche> findByName(String name, Pageable pageable) {
        return parcheRepository.findByNameContaining(name, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Parche> findParchesForMember(UUID userId, Pageable pageable) {
        return parcheRepository.findByMember(userId, pageable);
    }
}
