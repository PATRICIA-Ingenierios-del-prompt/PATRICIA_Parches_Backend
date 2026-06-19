package ingprompt.patricia.parches.infrastructure.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ingprompt.patricia.parches.application.dto.PresignedUpload;
import ingprompt.patricia.parches.application.port.out.PictureStorageOutPort;
import ingprompt.patricia.parches.domain.exception.InvalidPictureUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Component
public class S3PresignedPostAdapter implements PictureStorageOutPort {
    private static final String ALGORITHM = "AWS4-HMAC-SHA256";
    private static final String KEY_PREFIX = "parches/pictures/";
    private static final DateTimeFormatter AMZ_DATE = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.US).withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter DATE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.US).withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter POLICY_EXPIRATION = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).withZone(ZoneOffset.UTC);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String accessKeyId;
    private final String secretAccessKey;
    private final String sessionToken;
    private final String region;
    private final String bucket;
    private final String endpoint;
    private final String publicBaseUrl;
    private final long expirySeconds;

    public S3PresignedPostAdapter(@Value("${aws.access-key-id}") String accessKeyId, @Value("${aws.secret-access-key}") String secretAccessKey, @Value("${aws.session-token}") String sessionToken, @Value("${aws.s3.region}") String region, @Value("${aws.s3.bucket}") String bucket, @Value("${aws.s3.endpoint}") String endpoint, @Value("${aws.s3.public-base-url}") String publicBaseUrl, @Value("${parche.picture.upload-expiry-seconds}") long expirySeconds) {
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.sessionToken = sessionToken;
        this.region = region;
        this.bucket = bucket;
        this.endpoint = endpoint;
        this.publicBaseUrl = publicBaseUrl;
        this.expirySeconds = expirySeconds;
    }

    @Override
    public PresignedUpload generateImageUpload(String contentType, long maxBytes) {
        if (!StringUtils.hasText(accessKeyId) || !StringUtils.hasText(secretAccessKey)) {
            throw new InvalidPictureUploadException("Image storage is not configured (missing AWS credentials)");
        }

        String key = KEY_PREFIX + UUID.randomUUID() + "." + extensionFor(contentType);
        Instant now = Instant.now();
        String amzDate = AMZ_DATE.format(now);
        String dateStamp = DATE_STAMP.format(now);
        String credential = accessKeyId + "/" + dateStamp + "/" + region + "/s3/aws4_request";
        String expiration = POLICY_EXPIRATION.format(now.plusSeconds(expirySeconds));

        // ---- POST policy: every form field below (except "file" and the signature) must be a condition ----
        List<Object> conditions = new ArrayList<>();
        conditions.add(Map.of("bucket", bucket));
        conditions.add(List.of("eq", "$key", key));
        conditions.add(Map.of("acl", "public-read"));
        conditions.add(Map.of("Content-Type", contentType));
        conditions.add(List.of("content-length-range", 1, maxBytes));
        conditions.add(Map.of("x-amz-algorithm", ALGORITHM));
        conditions.add(Map.of("x-amz-credential", credential));
        conditions.add(Map.of("x-amz-date", amzDate));
        if (StringUtils.hasText(sessionToken)) {
            conditions.add(Map.of("x-amz-security-token", sessionToken));
        }

        Map<String, Object> policy = new LinkedHashMap<>();
        policy.put("expiration", expiration);
        policy.put("conditions", conditions);

        String base64Policy = Base64.getEncoder().encodeToString(toJson(policy).getBytes(StandardCharsets.UTF_8));
        String signature = hex(hmacSha256(signingKey(dateStamp), base64Policy));

        // ---- Form fields the client submits (file part LAST) ----
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("key", key);
        fields.put("acl", "public-read");
        fields.put("Content-Type", contentType);
        fields.put("x-amz-algorithm", ALGORITHM);
        fields.put("x-amz-credential", credential);
        fields.put("x-amz-date", amzDate);
        if (StringUtils.hasText(sessionToken)) {
            fields.put("x-amz-security-token", sessionToken);
        }
        fields.put("policy", base64Policy);
        fields.put("x-amz-signature", signature);

        return new PresignedUpload(uploadUrl(), fields, publicUrl(key), key);
    }

    private String uploadUrl() {
        // Path-style for an explicit endpoint (LocalStack/MinIO); virtual-hosted for real S3.
        return StringUtils.hasText(endpoint)
                ? stripTrailingSlash(endpoint) + "/" + bucket
                : "https://" + bucket + ".s3." + region + ".amazonaws.com";
    }

    private String publicUrl(String key) {
        if (StringUtils.hasText(publicBaseUrl)) {
            return stripTrailingSlash(publicBaseUrl) + "/" + key;
        }
        return uploadUrl() + "/" + key;
    }

    private byte[] signingKey(String dateStamp) {
        byte[] kSecret = ("AWS4" + secretAccessKey).getBytes(StandardCharsets.UTF_8);
        byte[] kDate = hmacSha256(kSecret, dateStamp);
        byte[] kRegion = hmacSha256(kDate, region);
        byte[] kService = hmacSha256(kRegion, "s3");
        return hmacSha256(kService, "aws4_request");
    }

    private static byte[] hmacSha256(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute HMAC-SHA256", e);
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize S3 POST policy", e);
        }
    }

    private static String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/jpeg" -> "jpg";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> "bin";
        };
    }
}
