package ingprompt.patricia.parches.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import ingprompt.patricia.parches.application.dto.PresignedUpload;
import ingprompt.patricia.parches.application.port.in.RequestPictureUploadCase;
import ingprompt.patricia.parches.domain.exception.InvalidPictureUploadException;
import ingprompt.patricia.parches.infrastructure.web.dto.request.PictureUploadRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PictureUploadController.class)
class PictureUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RequestPictureUploadCase requestPictureUploadCase;

    private final UUID userId = UUID.randomUUID();

    private PictureUploadRequest request(String contentType) {
        PictureUploadRequest r = new PictureUploadRequest();
        r.setContentType(contentType);
        r.setFileSize(1024L);
        return r;
    }

    @Test
    void requestUpload_withValidPayload_returns200() throws Exception {
        when(requestPictureUploadCase.requestParchePictureUpload(any(), any()))
                .thenReturn(new PresignedUpload("https://s3", Map.of("key", "v"), "https://public", "obj"));

        mockMvc.perform(post("/api/parches/picture-upload-url")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("image/png"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadUrl").value("https://s3"))
                .andExpect(jsonPath("$.objectKey").value("obj"));
    }

    @Test
    void requestUpload_withBlankContentType_returns400() throws Exception {
        mockMvc.perform(post("/api/parches/picture-upload-url")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("  "))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestUpload_whenServiceRejects_returns400() throws Exception {
        when(requestPictureUploadCase.requestParchePictureUpload(any(), any()))
                .thenThrow(new InvalidPictureUploadException("bad type"));

        mockMvc.perform(post("/api/parches/picture-upload-url")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("image/svg+xml"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void requestUpload_withoutHeader_returns400() throws Exception {
        mockMvc.perform(post("/api/parches/picture-upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("image/png"))))
                .andExpect(status().isBadRequest());
    }
}
