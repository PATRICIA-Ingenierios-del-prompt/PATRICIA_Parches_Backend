package ingprompt.patricia.parches.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PictureUploadRequest {
    @NotBlank
    private String contentType;
    private Long fileSize;
}
