package ingprompt.patricia.parches.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import ingprompt.patricia.parches.application.port.in.ReportParcheMemberCase;
import ingprompt.patricia.parches.domain.enums.ReportStatus;
import ingprompt.patricia.parches.domain.enums.ReportType;
import ingprompt.patricia.parches.domain.model.ParcheReportMember;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminReportController.class)
class AdminReportControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private ReportParcheMemberCase reportCase;

    private final UUID reportId = UUID.randomUUID();

    private ParcheReportMember sampleReport() {
        return new ParcheReportMember(reportId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                ReportType.SPAM, "spam", "Reported User", "Parche test", ReportStatus.PENDING, Instant.now(), null);
    }

    @Test
    void listAllReports_withAdminRole_returns200() throws Exception {
        when(reportCase.findAll(eq(null), eq("ADMIN"), any()))
                .thenReturn(new PageImpl<>(List.of(sampleReport())));

        mockMvc.perform(get("/api/parches/admin/reports")
                        .header("X-User-Roles", "ADMIN")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].reportId").value(reportId.toString()))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    void listAllReports_missingRolesHeader_returns400() throws Exception {
        mockMvc.perform(get("/api/parches/admin/reports"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resolveReport_withAdminRole_returns200() throws Exception {
        ParcheReportMember resolved = sampleReport();
        resolved.resolve();
        when(reportCase.resolveReport(reportId, "ADMIN")).thenReturn(resolved);

        mockMvc.perform(post("/api/parches/admin/reports/{reportId}/resolve", reportId)
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.resolvedAt").exists());
    }
}
