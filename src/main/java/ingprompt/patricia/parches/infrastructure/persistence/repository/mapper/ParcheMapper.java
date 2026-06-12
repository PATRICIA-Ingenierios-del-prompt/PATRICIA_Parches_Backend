package ingprompt.patricia.parches.infrastructure.persistence.repository.mapper;

import ingprompt.patricia.parches.domain.model.CollaborationTools;
import ingprompt.patricia.parches.domain.model.CommunicationChannels;
import ingprompt.patricia.parches.domain.model.Parche;
import ingprompt.patricia.parches.infrastructure.persistence.entity.ParcheEntity;

import java.util.HashSet;

public final class ParcheMapper {

    private ParcheMapper() {
    }

    public static ParcheEntity toEntity(Parche parche) {
        ParcheEntity entity = new ParcheEntity();
        entity.setParcheId(parche.getParcheId());
        entity.setName(parche.getName());
        entity.setDescription(parche.getDescription());
        entity.setOwnerId(parche.getOwnerId());
        entity.setCategory(parche.getCategory());
        entity.setVisibility(parche.getVisibility());
        entity.setStatus(parche.getStatus());
        entity.setMaxCapacity(parche.getMaxCapacity());
        entity.setMembers(new HashSet<>(parche.getMembers()));
        entity.setEvents(new HashSet<>(parche.getEvents()));
        entity.setCommunication(copy(parche.getCommunication()));
        entity.setCollabs(copy(parche.getCollabs()));
        return entity;
    }

    public static Parche toDomain(ParcheEntity entity) {
        Parche parche = new Parche(
                entity.getParcheId(),
                entity.getName(),
                entity.getCategory(),
                entity.getMaxCapacity(),
                entity.getOwnerId(),
                entity.getDescription(),
                entity.getVisibility()
        );
        parche.setStatus(entity.getStatus());
        parche.setMembers(entity.getMembers() == null ? new HashSet<>() : new HashSet<>(entity.getMembers()));
        parche.setEvents(entity.getEvents() == null ? new HashSet<>() : new HashSet<>(entity.getEvents()));
        parche.setCommunication(copy(entity.getCommunication()));
        parche.setCollabs(copy(entity.getCollabs()));
        return parche;
    }

    private static CommunicationChannels copy(CommunicationChannels source) {
        CommunicationChannels target = new CommunicationChannels();
        if (source != null) {
            target.setChatId(source.getChatId());
            target.setVoiceId(source.getVoiceId());
        }
        return target;
    }

    private static CollaborationTools copy(CollaborationTools source) {
        CollaborationTools target = new CollaborationTools();
        if (source != null) {
            target.setParquesId(source.getParquesId());
            target.setCanvasId(source.getCanvasId());
        }
        return target;
    }
}
