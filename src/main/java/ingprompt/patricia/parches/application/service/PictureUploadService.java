package ingprompt.patricia.parches.application.service;

import ingprompt.patricia.parches.application.dto.PresignedUpload;
import ingprompt.patricia.parches.application.port.in.RequestPictureUploadCase;
import ingprompt.patricia.parches.application.port.out.PictureStorageOutPort;
import ingprompt.patricia.parches.domain.exception.InvalidPictureUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PictureUploadService implements RequestPictureUploadCase {
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp",
            "image/gif"
    );

    private final PictureStorageOutPort pictureStorage;
    private final long maxBytes;

    public PictureUploadService(PictureStorageOutPort pictureStorage, @Value("${parche.picture.max-bytes}") long maxBytes) {
        this.pictureStorage = pictureStorage;
        this.maxBytes = maxBytes;
    }

    @Override
    public PresignedUpload requestParchePictureUpload(String contentType, Long declaredSizeBytes) {
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidPictureUploadException(
                    "Unsupported image type. Allowed: " + ALLOWED_TYPES);
        }
        if (declaredSizeBytes != null && (declaredSizeBytes <= 0 || declaredSizeBytes > maxBytes)) {
            throw new InvalidPictureUploadException(
                    "Image must be between 1 byte and " + maxBytes + " bytes");
        }
        return pictureStorage.generateImageUpload(contentType.toLowerCase(), maxBytes);
    }
}
