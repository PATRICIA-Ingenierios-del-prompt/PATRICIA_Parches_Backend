package ingprompt.patricia.parches.application.dto;

import java.util.Map;

public record PresignedUpload(String uploadUrl, Map<String, String> fields, String publicUrl, String objectKey) {
}
