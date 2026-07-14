package ingprompt.patricia.parches.infrastructure.storage;

import ingprompt.patricia.parches.application.dto.PresignedUpload;
import ingprompt.patricia.parches.domain.exception.InvalidPictureUploadException;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class S3PresignedPostAdapterTest {

    private static final long MAX_BYTES = 5_000_000L;
    private static final AwsCredentialsProvider BASIC =
            StaticCredentialsProvider.create(AwsBasicCredentials.create("AKIATEST", "secret"));

    private S3PresignedPostAdapter adapter(AwsCredentialsProvider credentials,
                                           String endpoint, String publicBaseUrl) {
        return new S3PresignedPostAdapter(credentials,
                "us-east-1", "patricia-pics", endpoint, publicBaseUrl, 300L);
    }

    @Test
    void generateImageUpload_buildsSignedPostForRealS3() {
        S3PresignedPostAdapter adapter = adapter(BASIC, "", "");

        PresignedUpload upload = adapter.generateImageUpload("image/png", MAX_BYTES);

        assertThat(upload.uploadUrl()).isEqualTo("https://patricia-pics.s3.us-east-1.amazonaws.com");
        assertThat(upload.objectKey()).startsWith("parches/pictures/").endsWith(".png");
        assertThat(upload.publicUrl()).isEqualTo(upload.uploadUrl() + "/" + upload.objectKey());
        assertThat(upload.fields())
                .containsKeys("key", "Content-Type", "x-amz-algorithm",
                        "x-amz-credential", "x-amz-date", "policy", "x-amz-signature");
        assertThat(upload.fields().get("Content-Type")).isEqualTo("image/png");
        assertThat(upload.fields().get("x-amz-credential")).endsWith("/us-east-1/s3/aws4_request");
        // Bucket blocks public ACLs; the policy must not carry one.
        assertThat(upload.fields()).doesNotContainKey("acl");
        assertThat(upload.fields()).doesNotContainKey("x-amz-security-token");
    }

    @Test
    void generateImageUpload_usesPathStyleAndPublicBaseUrlWhenConfigured() {
        S3PresignedPostAdapter adapter = adapter(BASIC,
                "http://localhost:4566/", "https://cdn.patricia.app/");

        PresignedUpload upload = adapter.generateImageUpload("image/jpeg", MAX_BYTES);

        assertThat(upload.uploadUrl()).isEqualTo("http://localhost:4566/patricia-pics");
        assertThat(upload.objectKey()).endsWith(".jpg");
        assertThat(upload.publicUrl()).startsWith("https://cdn.patricia.app/parches/pictures/");
    }

    @Test
    void generateImageUpload_includesSecurityTokenWhenPresent() {
        S3PresignedPostAdapter adapter = adapter(StaticCredentialsProvider.create(
                AwsSessionCredentials.create("AKIATEST", "secret", "SESSIONTOKEN")), "", "");

        PresignedUpload upload = adapter.generateImageUpload("image/webp", MAX_BYTES);

        assertThat(upload.fields()).containsEntry("x-amz-security-token", "SESSIONTOKEN");
        assertThat(upload.objectKey()).endsWith(".webp");
    }

    @Test
    void generateImageUpload_mapsGifExtension() {
        S3PresignedPostAdapter adapter = adapter(BASIC, "", "");

        PresignedUpload upload = adapter.generateImageUpload("image/gif", MAX_BYTES);

        assertThat(upload.objectKey()).endsWith(".gif");
    }

    @Test
    void generateImageUpload_withoutCredentials_throws() {
        AwsCredentialsProvider failing = () -> {
            throw SdkClientException.create("Unable to load credentials");
        };
        S3PresignedPostAdapter adapter = adapter(failing, "", "");

        assertThatThrownBy(() -> adapter.generateImageUpload("image/png", MAX_BYTES))
                .isInstanceOf(InvalidPictureUploadException.class);
    }

    @Test
    void generateImageUpload_withoutBucket_throws() {
        S3PresignedPostAdapter adapter = new S3PresignedPostAdapter(BASIC,
                "us-east-1", "", "", "", 300L);

        assertThatThrownBy(() -> adapter.generateImageUpload("image/png", MAX_BYTES))
                .isInstanceOf(InvalidPictureUploadException.class);
    }
}
