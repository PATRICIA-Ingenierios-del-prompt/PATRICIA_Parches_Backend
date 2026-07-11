package ingprompt.patricia.parches.application.service;

import ingprompt.patricia.parches.application.port.out.ParcheRepositoryOutPort;
import ingprompt.patricia.parches.application.port.out.ReportRepositoryOutPort;
import ingprompt.patricia.parches.domain.enums.ParcheCategory;
import ingprompt.patricia.parches.domain.enums.ReportType;
import ingprompt.patricia.parches.domain.enums.Visibility;
import ingprompt.patricia.parches.domain.exception.InvalidReportException;
import ingprompt.patricia.parches.domain.exception.ParcheNotFoundException;
import ingprompt.patricia.parches.domain.exception.UnauthorizedReportAccessException;
import ingprompt.patricia.parches.domain.model.Parche;
import ingprompt.patricia.parches.domain.model.ParcheReportMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private ReportRepositoryOutPort reportRepository;
    @Mock private ParcheRepositoryOutPort parcheRepository;

    @InjectMocks
    private ReportService service;

    private UUID parcheId;
    private UUID ownerId;
    private UUID creatorId;
    private UUID reportedId;
    private UUID randomUserId;
    private Parche parche;

    @BeforeEach
    void setUp() {
        parcheId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        creatorId = UUID.randomUUID();
        reportedId = UUID.randomUUID();
        randomUserId = UUID.randomUUID();
        parche = new Parche(parcheId, "Salsa night", ParcheCategory.MUSIC, 10, ownerId, "desc", Visibility.PUBLIC);
        parche.addMember(creatorId);
        parche.addMember(reportedId);
    }

    // ---------- reportMember ---------------------------------------------

    @Test
    void reportMember_persistsAndReturnsDomain_withGeneratedIdAndTimestamp() {
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        Instant beforeCall = Instant.now().minusMillis(1);

        ParcheReportMember result = service.reportMember(parcheId, creatorId, reportedId, ReportType.SPAM, "molesta");

        ArgumentCaptor<ParcheReportMember> captor = ArgumentCaptor.forClass(ParcheReportMember.class);
        verify(reportRepository).save(captor.capture());
        ParcheReportMember saved = captor.getValue();
        assertThat(saved).isSameAs(result);
        assertThat(saved.getReportId()).isNotNull();
        assertThat(saved.getParcheId()).isEqualTo(parcheId);
        assertThat(saved.getCreatorId()).isEqualTo(creatorId);
        assertThat(saved.getReportedId()).isEqualTo(reportedId);
        assertThat(saved.getReportType()).isEqualTo(ReportType.SPAM);
        assertThat(saved.getDescription()).isEqualTo("molesta");
        assertThat(saved.getCreatedAt()).isAfterOrEqualTo(beforeCall);
    }

    @Test
    void reportMember_selfReport_throws() {
        assertThatThrownBy(() -> service.reportMember(parcheId, creatorId, creatorId, ReportType.SPAM, null))
                .isInstanceOf(InvalidReportException.class);
        verify(reportRepository, never()).save(any());
        verify(parcheRepository, never()).findById(any());
    }

    @Test
    void reportMember_parcheNotFound_throws() {
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.reportMember(parcheId, creatorId, reportedId, ReportType.SPAM, null))
                .isInstanceOf(ParcheNotFoundException.class);
        verify(reportRepository, never()).save(any());
    }

    @Test
    void reportMember_creatorNotAMember_throws() {
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        assertThatThrownBy(() -> service.reportMember(parcheId, randomUserId, reportedId, ReportType.SPAM, null))
                .isInstanceOf(InvalidReportException.class);
        verify(reportRepository, never()).save(any());
    }

    @Test
    void reportMember_reportedNotAMember_throws() {
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        assertThatThrownBy(() -> service.reportMember(parcheId, creatorId, randomUserId, ReportType.SPAM, null))
                .isInstanceOf(InvalidReportException.class);
        verify(reportRepository, never()).save(any());
    }

    // ---------- findByParcheId (authorization) ---------------------------

    @Test
    void findByParcheId_asOwner_returnsPage() {
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        Pageable pageable = PageRequest.of(0, 10);
        Page<ParcheReportMember> page = new PageImpl<>(List.of(sampleReport()));
        when(reportRepository.findByParcheId(parcheId, pageable)).thenReturn(page);

        Page<ParcheReportMember> result = service.findByParcheId(parcheId, ownerId, "STUDENT", pageable);

        assertThat(result).isSameAs(page);
    }

    @Test
    void findByParcheId_asAdmin_bypassesOwnerCheck() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ParcheReportMember> page = new PageImpl<>(List.of(sampleReport()));
        when(reportRepository.findByParcheId(parcheId, pageable)).thenReturn(page);

        Page<ParcheReportMember> result = service.findByParcheId(parcheId, randomUserId, "STUDENT,ADMIN", pageable);

        assertThat(result).isSameAs(page);
        // admin path must NOT hit the parche repository (short-circuits before the owner check)
        verify(parcheRepository, never()).findById(any());
    }

    @Test
    void findByParcheId_neitherOwnerNorAdmin_throws() {
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        assertThatThrownBy(() -> service.findByParcheId(parcheId, randomUserId, "STUDENT", PageRequest.of(0, 10)))
                .isInstanceOf(UnauthorizedReportAccessException.class);
        verify(reportRepository, never()).findByParcheId(any(), any());
    }

    @Test
    void findByParcheId_nullRoles_stillEnforcesOwnerCheck() {
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        assertThatThrownBy(() -> service.findByParcheId(parcheId, randomUserId, null, PageRequest.of(0, 10)))
                .isInstanceOf(UnauthorizedReportAccessException.class);
    }

    @Test
    void findByParcheId_parcheNotFound_throws() {
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findByParcheId(parcheId, ownerId, "STUDENT", PageRequest.of(0, 10)))
                .isInstanceOf(ParcheNotFoundException.class);
    }

    // ---------- findById (single report) --------------------------------

    @Test
    void findById_asOwner_returnsReport() {
        UUID reportId = UUID.randomUUID();
        ParcheReportMember report = new ParcheReportMember(reportId, parcheId, creatorId, reportedId,
                ReportType.SPAM, "x", Instant.now());
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        ParcheReportMember result = service.findById(parcheId, reportId, ownerId, "STUDENT");

        assertThat(result).isSameAs(report);
    }

    @Test
    void findById_asAdmin_returnsReport() {
        UUID reportId = UUID.randomUUID();
        ParcheReportMember report = new ParcheReportMember(reportId, parcheId, creatorId, reportedId,
                ReportType.SPAM, "x", Instant.now());
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        ParcheReportMember result = service.findById(parcheId, reportId, randomUserId, "ADMIN");

        assertThat(result).isSameAs(report);
        verify(parcheRepository, never()).findById(any());
    }

    @Test
    void findById_forbidden_throwsBeforeTouchingReport() {
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        assertThatThrownBy(() -> service.findById(parcheId, UUID.randomUUID(), randomUserId, "STUDENT"))
                .isInstanceOf(UnauthorizedReportAccessException.class);
        verify(reportRepository, never()).findById(any());
    }

    @Test
    void findById_reportBelongsToDifferentParche_isBlockedAsInvalid() {
        UUID reportId = UUID.randomUUID();
        UUID otherParcheId = UUID.randomUUID();
        ParcheReportMember report = new ParcheReportMember(reportId, otherParcheId, creatorId, reportedId,
                ReportType.SPAM, "x", Instant.now());
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> service.findById(parcheId, reportId, ownerId, "STUDENT"))
                .isInstanceOf(InvalidReportException.class);
    }

    @Test
    void findById_reportNotFound_throws() {
        UUID reportId = UUID.randomUUID();
        when(parcheRepository.findById(parcheId)).thenReturn(Optional.of(parche));
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(parcheId, reportId, ownerId, "STUDENT"))
                .isInstanceOf(InvalidReportException.class);
    }

    private ParcheReportMember sampleReport() {
        return new ParcheReportMember(UUID.randomUUID(), parcheId, creatorId, reportedId,
                ReportType.SPAM, "x", Instant.now());
    }
}
