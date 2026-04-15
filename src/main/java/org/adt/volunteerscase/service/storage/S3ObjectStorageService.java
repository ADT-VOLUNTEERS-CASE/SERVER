package org.adt.volunteerscase.service.storage;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.config.s3.S3Properties;
import org.adt.volunteerscase.exception.CoverUploadException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ObjectStorageService implements ObjectStorageService {

    private final S3Client s3Client;
    private final S3Properties properties;

    @Override
    public StoredObjectResult uploadCover(String originalFileName, String contentType, byte[]
            content) {
        String safeFileName = sanitizeFileName(originalFileName);
        String objectKey = buildObjectKey(safeFileName);

        try {
            PutObjectResponse response = s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(properties.getBucket())
                            .key(objectKey)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(content)
            );

            return StoredObjectResult.builder()
                    .bucket(properties.getBucket())
                    .objectKey(objectKey)
                    .link(buildPublicLink(objectKey))
                    .eTag(response.eTag())
                    .build();
        } catch (S3Exception ex) {
            throw new CoverUploadException("cannot upload cover to S3", ex);        }
    }

    @Override
    public void deleteObject(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return;
        }

        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(properties.getBucket())
                            .key(objectKey)
                            .build()
            );
        } catch (S3Exception ex) {
            throw new CoverUploadException("cannot upload cover to S3", ex);        }
    }

    private String buildObjectKey(String fileName) {
        String folder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        return properties.getCoverPrefix() + "/" + folder + "/" + UUID.randomUUID() + "-" +
                fileName;
    }

    private String buildPublicLink(String objectKey) {
        if (!StringUtils.hasText(properties.getPublicBaseUrl())) {
            throw new CoverUploadException("storage.s3.public-base-url must be configured");
        }

        String baseUrl = StringUtils.trimTrailingCharacter(properties.getPublicBaseUrl(), '/');
        return baseUrl + "/" + objectKey;
    }

    private String sanitizeFileName(String originalFileName) {
        String fileName = StringUtils.hasText(originalFileName) ? originalFileName : "cover";
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
