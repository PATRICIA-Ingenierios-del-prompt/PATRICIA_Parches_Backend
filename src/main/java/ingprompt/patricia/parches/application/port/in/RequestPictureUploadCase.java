package ingprompt.patricia.parches.application.port.in;

import ingprompt.patricia.parches.application.dto.PresignedUpload;

public interface RequestPictureUploadCase {
    PresignedUpload requestParchePictureUpload(String contentType, Long declaredSizeBytes);
}
