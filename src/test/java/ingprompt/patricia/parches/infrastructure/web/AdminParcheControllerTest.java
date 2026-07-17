package ingprompt.patricia.parches.infrastructure.web;

import ingprompt.patricia.parches.application.port.in.ParcheAdminCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminParcheController.class)
class AdminParcheControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ParcheAdminCase parcheAdminCase;

    @Test
    void stats_withAdminRole_returnsTotalParches() throws Exception {
        when(parcheAdminCase.countParches()).thenReturn(15L);

        mockMvc.perform(get("/api/parches/admin/stats")
                        .header("X-User-Roles", "ADMIN")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalParches").value(15));
    }

    @Test
    void stats_withNonAdminRole_returns403() throws Exception {
        when(parcheAdminCase.countParches()).thenReturn(15L);

        mockMvc.perform(get("/api/parches/admin/stats")
                        .header("X-User-Roles", "STUDENT"))
                .andExpect(status().isForbidden());
    }

    @Test
    void stats_missingRolesHeader_returns400() throws Exception {
        mockMvc.perform(get("/api/parches/admin/stats"))
                .andExpect(status().isBadRequest());
    }
}
