package ingprompt.patricia.parches.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import ingprompt.patricia.parches.application.port.in.ManageMemberParcheCase;
import ingprompt.patricia.parches.application.port.in.ManageParcheCase;
import ingprompt.patricia.parches.application.port.in.ParcheQueryCase;
import ingprompt.patricia.parches.application.port.in.SpecialQueriesFilterCases;
import ingprompt.patricia.parches.domain.enums.ParcheCategory;
import ingprompt.patricia.parches.domain.enums.Visibility;
import ingprompt.patricia.parches.domain.exception.ParcheNotFoundException;
import ingprompt.patricia.parches.domain.model.Parche;
import ingprompt.patricia.parches.infrastructure.web.dto.request.CreateParcheRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParcheController.class)
class ParcheControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ManageParcheCase manageParche;
    @MockitoBean
    private ManageMemberParcheCase manageMember;
    @MockitoBean
    private ParcheQueryCase parcheQuery;
    @MockitoBean
    private SpecialQueriesFilterCases filter;

    private final UUID userId = UUID.randomUUID();
    private final UUID parcheId = UUID.randomUUID();

    private Parche sampleParche() {
        return new Parche(parcheId, "Salsa night", ParcheCategory.MUSIC, 10, userId, "desc", Visibility.PUBLIC);
    }

    @Test
    void createParche_withHeader_returns200() throws Exception {
        CreateParcheRequest request = new CreateParcheRequest();
        request.setName("Salsa night");
        request.setCategory(ParcheCategory.MUSIC);
        request.setMaxCapacity(10);
        request.setVisibility(Visibility.PUBLIC);
        when(manageParche.createParche(any(), any(), any(int.class), any(), any(), any(), any()))
                .thenReturn(sampleParche());

        mockMvc.perform(post("/api/parches")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Salsa night"));
    }

    @Test
    void createParche_withoutHeader_returns400() throws Exception {
        mockMvc.perform(post("/api/parches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteParche_returns204() throws Exception {
        mockMvc.perform(delete("/api/parches/{id}", parcheId).header("X-User-Id", userId))
                .andExpect(status().isNoContent());
        verify(manageParche).deleteParche(parcheId, userId);
    }

    @Test
    void joinParche_returns204() throws Exception {
        mockMvc.perform(post("/api/parches/{id}/join", parcheId).header("X-User-Id", userId))
                .andExpect(status().isNoContent());
        verify(manageMember).joinPublicParche(parcheId, userId);
    }

    @Test
    void removeMember_returns204() throws Exception {
        UUID member = UUID.randomUUID();
        mockMvc.perform(delete("/api/parches/{id}/members/{memberId}", parcheId, member).header("X-User-Id", userId))
                .andExpect(status().isNoContent());
        verify(manageMember).removeMemberFromParche(parcheId, member, userId);
    }

    @Test
    void getParche_returns200() throws Exception {
        when(parcheQuery.getParcheById(parcheId)).thenReturn(sampleParche());

        mockMvc.perform(get("/api/parches/{id}", parcheId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Salsa night"))
                .andExpect(jsonPath("$.memberCount").value(1));
    }

    @Test
    void getParche_whenMissing_returns404() throws Exception {
        when(parcheQuery.getParcheById(parcheId)).thenThrow(new ParcheNotFoundException(parcheId));

        mockMvc.perform(get("/api/parches/{id}", parcheId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getParcheEvents_returns200() throws Exception {
        when(parcheQuery.getEventsOfParche(parcheId)).thenReturn(java.util.Set.of(UUID.randomUUID()));

        mockMvc.perform(get("/api/parches/{id}/events", parcheId))
                .andExpect(status().isOk());
    }

    @Test
    void filterByCategory_withHeader_returns200() throws Exception {
        when(filter.filterByCategory(eq(ParcheCategory.MUSIC), any()))
                .thenReturn(new PageImpl<>(List.of(sampleParche())));

        mockMvc.perform(get("/api/parches/category").param("category", "MUSIC").header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].parcheId").value(parcheId.toString()));
    }

    @Test
    void filterByCategory_withoutHeader_returns400() throws Exception {
        mockMvc.perform(get("/api/parches/category").param("category", "MUSIC"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void filterByOpenSpots_returns200() throws Exception {
        when(filter.filterByOpenSpots(any())).thenReturn(new PageImpl<>(List.of(sampleParche())));

        mockMvc.perform(get("/api/parches/capacity").header("X-User-Id", userId))
                .andExpect(status().isOk());
    }

    @Test
    void filterByName_returns200() throws Exception {
        when(filter.findByName(eq("salsa"), any())).thenReturn(new PageImpl<>(List.of(sampleParche())));

        mockMvc.perform(get("/api/parches/name").param("name", "salsa").header("X-User-Id", userId))
                .andExpect(status().isOk());
    }

    @Test
    void filterByVisibility_returns200() throws Exception {
        when(filter.filterByVisibility(eq(Visibility.PUBLIC), any())).thenReturn(new PageImpl<>(List.of(sampleParche())));

        mockMvc.perform(get("/api/parches/visibility").param("visibility", "PUBLIC").header("X-User-Id", userId))
                .andExpect(status().isOk());
    }
}
