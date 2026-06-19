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
    /** POST the multipart form here (fields first, the "file" part last). */
    private String uploadUrl;
    /** Form fields to include before the file part. */
    private Map<String, String> fields;
    /** Send this back as pictureUrl when creating the parche. */
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
