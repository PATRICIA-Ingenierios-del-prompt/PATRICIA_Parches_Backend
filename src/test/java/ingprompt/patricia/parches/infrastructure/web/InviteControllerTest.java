package ingprompt.patricia.parches.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import ingprompt.patricia.parches.application.port.in.InviteUserCase;
import ingprompt.patricia.parches.domain.exception.InvalidInviteTokenException;
import ingprompt.patricia.parches.infrastructure.web.dto.request.CreateInviteRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InviteController.class)
class InviteControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InviteUserCase inviteUserCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID parcheId = UUID.randomUUID();

    @Test
    void invite_withHeader_returnsToken() throws Exception {
        CreateInviteRequest request = new CreateInviteRequest();
        request.setParcheId(parcheId);
        when(inviteUserCase.inviteUser(parcheId, userId)).thenReturn("ABCD-EFGH");

        mockMvc.perform(post("/api/invites")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("ABCD-EFGH"));
    }

    @Test
    void invite_withoutHeader_returns400() throws Exception {
        mockMvc.perform(post("/api/invites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void accept_returns204() throws Exception {
        mockMvc.perform(post("/api/invites/accept").param("token", "ABCD-EFGH").header("X-User-Id", userId))
                .andExpect(status().isNoContent());
        verify(inviteUserCase).acceptInvite("ABCD-EFGH", userId);
    }

    @Test
    void accept_withInvalidToken_returns401() throws Exception {
        doThrow(new InvalidInviteTokenException()).when(inviteUserCase).acceptInvite(eq("BAD"), eq(userId));

        mockMvc.perform(post("/api/invites/accept").param("token", "BAD").header("X-User-Id", userId))
                .andExpect(status().isUnauthorized());
    }
}
