package ingprompt.patricia.parches.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import ingprompt.patricia.parches.application.port.in.ReportParcheMemberCase;
import ingprompt.patricia.parches.domain.enums.ReportType;
import ingprompt.patricia.parches.domain.exception.InvalidReportException;
import ingprompt.patricia.parches.domain.exception.UnauthorizedReportAccessException;
import ingprompt.patricia.parches.domain.model.ParcheReportMember;
import ingprompt.patricia.parches.infrastructure.web.dto.request.CreateReportRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private ReportParcheMemberCase reportCase;

    private final UUID parcheId  = UUID.randomUUID();
    private final UUID creatorId = UUID.randomUUID();
    private final UUID reportedId = UUID.randomUUID();
    private final UUID reportId  = UUID.randomUUID();

    private CreateReportRequest requestBody() {
        CreateReportRequest r = new CreateReportRequest();
        r.setReportedId(reportedId);
        r.setReportType(ReportType.SPAM);
        r.setDescription("molesta");
        return r;
    }

    private ParcheReportMember sampleReport() {
        return new ParcheReportMember(reportId, parcheId, creatorId, reportedId, ReportType.SPAM, "molesta", Instant.now());
    }

    @Test
    void createReport_returns200_andBody() throws Exception {
        when(reportCase.reportMember(eq(parcheId), eq(creatorId), eq(reportedId), eq(ReportType.SPAM), eq("molesta")))
                .thenReturn(sampleReport());

        mockMvc.perform(post("/api/parches/{parcheId}/reports", parcheId)
                        .header("X-User-Id", creatorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value(reportId.toString()))
                .andExpect(jsonPath("$.parcheId").value(parcheId.toString()))
                .andExpect(jsonPath("$.creatorId").value(creatorId.toString()))
                .andExpect(jsonPath("$.reportedId").value(reportedId.toString()))
                .andExpect(jsonPath("$.reportType").value("SPAM"));
    }

    @Test
    void createReport_selfReport_returns400() throws Exception {
        when(reportCase.reportMember(any(), any(), any(), any(), any()))
                .thenThrow(new InvalidReportException("A user cannot report themselves"));

        mockMvc.perform(post("/api/parches/{parcheId}/reports", parcheId)
                        .header("X-User-Id", creatorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createReport_missingXUserId_returns400() throws Exception {
        mockMvc.perform(post("/api/parches/{parcheId}/reports", parcheId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReports_returns200_andPage() throws Exception {
        when(reportCase.findByParcheId(eq(parcheId), eq(creatorId), eq("STUDENT"), any()))
                .thenReturn(new PageImpl<>(List.of(sampleReport())));

        mockMvc.perform(get("/api/parches/{parcheId}/reports", parcheId)
                        .header("X-User-Id", creatorId)
                        .header("X-User-Roles", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].reportId").value(reportId.toString()));
    }

    @Test
    void listReports_forbidden_returns403() throws Exception {
        UUID stranger = UUID.randomUUID();
        when(reportCase.findByParcheId(eq(parcheId), eq(stranger), eq("STUDENT"), any()))
                .thenThrow(new UnauthorizedReportAccessException(stranger, "STUDENT"));

        mockMvc.perform(get("/api/parches/{parcheId}/reports", parcheId)
                        .header("X-User-Id", stranger)
                        .header("X-User-Roles", "STUDENT"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void listReports_missingRolesHeader_returns400() throws Exception {
        mockMvc.perform(get("/api/parches/{parcheId}/reports", parcheId)
                        .header("X-User-Id", creatorId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReport_asAdmin_returns200() throws Exception {
        UUID stranger = UUID.randomUUID();
        when(reportCase.findById(eq(parcheId), eq(reportId), eq(stranger), eq("STUDENT,ADMIN")))
                .thenReturn(sampleReport());

        mockMvc.perform(get("/api/parches/{parcheId}/reports/{reportId}", parcheId, reportId)
                        .header("X-User-Id", stranger)
                        .header("X-User-Roles", "STUDENT,ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value(reportId.toString()));
    }

    @Test
    void getReport_wrongParche_returns400() throws Exception {
        when(reportCase.findById(eq(parcheId), eq(reportId), any(), any()))
                .thenThrow(new InvalidReportException("Report " + reportId + " does not belong to parche " + parcheId));

        mockMvc.perform(get("/api/parches/{parcheId}/reports/{reportId}", parcheId, reportId)
                        .header("X-User-Id", creatorId)
                        .header("X-User-Roles", "STUDENT"))
                .andExpect(status().isBadRequest());
    }
}
