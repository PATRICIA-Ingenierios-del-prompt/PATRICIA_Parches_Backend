package ingprompt.patricia.parches.infrastructure.web;

import ingprompt.patricia.parches.application.dto.PresignedUpload;
import ingprompt.patricia.parches.application.port.in.RequestPictureUploadCase;
import ingprompt.patricia.parches.infrastructure.web.dto.request.PictureUploadRequest;
import ingprompt.patricia.parches.infrastructure.web.dto.response.PictureUploadResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/parches/picture-upload-url")
@AllArgsConstructor
public class PictureUploadController {
    private final RequestPictureUploadCase requestPictureUploadCase;

    @PostMapping
    public ResponseEntity<PictureUploadResponse> requestUpload(@Valid @RequestBody PictureUploadRequest request, @RequestHeader("X-User-Id") UUID requesterId) {
        PresignedUpload upload = requestPictureUploadCase.requestParchePictureUpload(request.getContentType(), request.getFileSize());
        return ResponseEntity.ok(PictureUploadResponse.from(upload));
    }
}
