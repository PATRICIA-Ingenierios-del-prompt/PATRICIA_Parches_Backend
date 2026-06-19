package ingprompt.patricia.parches.infrastructure.web.dto.response;

import ingprompt.patricia.parches.application.dto.PresignedUpload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PictureUploadResponse {
    private String uploadUrl;
    private Map<String, String> fields;
    private String publicUrl;
    private String objectKey;

    public static PictureUploadResponse from(PresignedUpload upload) {
        return new PictureUploadResponse(
                upload.uploadUrl(),
                upload.fields(),
                upload.publicUrl(),
                upload.objectKey());
    }
}
