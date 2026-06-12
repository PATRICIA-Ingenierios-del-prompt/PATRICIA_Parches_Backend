package ingprompt.patricia.parches.domain.model;

import ingprompt.patricia.parches.domain.enums.ParcheCategory;
import ingprompt.patricia.parches.domain.enums.ParcheStatus;
import ingprompt.patricia.parches.domain.enums.Visibility;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class Parche {
    private UUID parcheId;
    private String name;
    private String description;
    private UUID ownerId;
    private ParcheCategory category;
    private Visibility visibility;
    private int maxCapacity;
    private ParcheStatus status;
    private Set<UUID> members;

    private Set<UUID> events;
    private CommunicationChannels communication;
    private CollaborationTools collabs;

    public Parche(UUID parcheId, String name, ParcheCategory category, int num, UUID ownerId, String description, Visibility visibility) {
        this.parcheId = parcheId;
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.category = category;
        this.visibility = visibility;
        this.maxCapacity = num;
        this.status = ParcheStatus.PENDING_PROVISIONING;
        this.members = new HashSet<>();
        this.members.add(ownerId);

        this.events = new HashSet<>();
        this.communication = new CommunicationChannels();
        this.collabs = new CollaborationTools();
    }

    public void addMember(UUID memberId) {
        members.add(memberId);
    }
    public void deleteMember(UUID memberId){
        members.remove(memberId);
    }

    public void addEvent(UUID eventId) {
        events.add(eventId);
    }

    public void assignCommunication(UUID chatId, UUID voiceId) {
        this.communication.setChatId(chatId);
        this.communication.setVoiceId(voiceId);
        refreshStatus();
    }
    public void assignCollabs(UUID parquesId, UUID canvasId) {
        this.collabs.setParquesId(parquesId);
        this.collabs.setCanvasId(canvasId);
        refreshStatus();
    }

    public void rotateParquesId(UUID nuevoParquesId) {
        this.collabs.setParquesId(nuevoParquesId);
    }
    public void rotateCanvasId(UUID nuevoCanvasId) {
        this.collabs.setCanvasId(nuevoCanvasId);
    }

    public boolean isOwnedBy(UUID userId) {
        return this.ownerId.equals(userId);
    }

    public boolean isPublic() {
        return this.visibility == Visibility.PUBLIC;
    }

    public boolean isPrivate() {
        return this.visibility == Visibility.PRIVATE;
    }

    public boolean isFull() {
        return this.members.size() >= this.maxCapacity;
    }

    public boolean hasMember(UUID userId) {
        return this.members.contains(userId);
    }

    private void refreshStatus() {
        if (communication.isReady() && collabs.isReady()) {
            this.status = ParcheStatus.READY;
        }
    }
}
