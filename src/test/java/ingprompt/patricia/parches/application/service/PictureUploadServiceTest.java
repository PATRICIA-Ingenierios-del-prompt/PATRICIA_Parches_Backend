package ingprompt.patricia.parches.application.service;

import ingprompt.patricia.parches.application.dto.PresignedUpload;
import ingprompt.patricia.parches.application.port.out.PictureStorageOutPort;
import ingprompt.patricia.parches.domain.exception.InvalidPictureUploadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PictureUploadServiceTest {

    private static final long MAX_BYTES = 5_000_000L;

    @Mock
    private PictureStorageOutPort pictureStorage;

    private PictureUploadService service;

    @BeforeEach
    void setUp() {
        service = new PictureUploadService(pictureStorage, MAX_BYTES);
    }

    @Test
    void requestUpload_withAllowedType_delegatesToStorage() {
        PresignedUpload expected = new PresignedUpload("https://s3", Map.of("key", "v"), "https://public", "obj");
        when(pictureStorage.generateImageUpload(eq("image/png"), anyLong())).thenReturn(expected);

        PresignedUpload result = service.requestParchePictureUpload("image/png", 1024L);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void requestUpload_normalizesContentTypeCasing() {
        when(pictureStorage.generateImageUpload(eq("image/jpeg"), anyLong()))
                .thenReturn(new PresignedUpload("u", Map.of(), "p", "o"));

        service.requestParchePictureUpload("IMAGE/JPEG", null);

        verify(pictureStorage).generateImageUpload(eq("image/jpeg"), anyLong());
    }

    @Test
    void requestUpload_withUnsupportedType_throws() {
        assertThatThrownBy(() -> service.requestParchePictureUpload("image/svg+xml", 1024L))
                .isInstanceOf(InvalidPictureUploadException.class);
        verify(pictureStorage, never()).generateImageUpload(org.mockito.ArgumentMatchers.any(), anyLong());
    }

    @Test
    void requestUpload_withNullType_throws() {
        assertThatThrownBy(() -> service.requestParchePictureUpload(null, 1024L))
                .isInstanceOf(InvalidPictureUploadException.class);
    }

    @Test
    void requestUpload_withOversizedImage_throws() {
        assertThatThrownBy(() -> service.requestParchePictureUpload("image/png", MAX_BYTES + 1))
                .isInstanceOf(InvalidPictureUploadException.class);
    }

    @Test
    void requestUpload_withZeroSize_throws() {
        assertThatThrownBy(() -> service.requestParchePictureUpload("image/png", 0L))
                .isInstanceOf(InvalidPictureUploadException.class);
    }
}
