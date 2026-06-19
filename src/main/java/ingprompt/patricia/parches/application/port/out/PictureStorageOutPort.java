package ingprompt.patricia.parches.application.port.out;

import ingprompt.patricia.parches.application.dto.PresignedUpload;

public interface PictureStorageOutPort {
    PresignedUpload generateImageUpload(String contentType, long maxBytes);
}
