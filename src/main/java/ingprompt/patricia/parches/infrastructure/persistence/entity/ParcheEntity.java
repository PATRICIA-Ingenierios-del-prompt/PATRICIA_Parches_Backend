package ingprompt.patricia.parches.infrastructure.persistence.entity;

import ingprompt.patricia.parches.domain.enums.ParcheCategory;
import ingprompt.patricia.parches.domain.enums.ParcheStatus;
import ingprompt.patricia.parches.domain.enums.Visibility;
import ingprompt.patricia.parches.domain.model.CollaborationTools;
import ingprompt.patricia.parches.domain.model.CommunicationChannels;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Table(name = "parches")
public class ParcheEntity {
    @Id
    private UUID parcheId;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParcheCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParcheStatus status;

    private int maxCapacity;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "parche_members", joinColumns = @JoinColumn(name = "parche_id"))
    @Column(name = "member_id")
    private Set<UUID> members;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "parche_events", joinColumns = @JoinColumn(name = "parche_id"))
    @Column(name = "event_id")
    private Set<UUID> events;

    @Embedded
    private CommunicationChannels communication;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "parquesId", column = @Column(name = "collabs_parques_id")),
            @AttributeOverride(name = "canvasId", column = @Column(name = "collabs_canvas_id"))
    })
    private CollaborationTools collabs;
}
