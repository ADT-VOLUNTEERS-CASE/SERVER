package org.adt.volunteerscase.config.s3;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@ConfigurationProperties(prefix = "storage.s3")
@Validated
@ToString(exclude = {"accessKey", "secretKey"})
public class S3Properties {
    private String endpoint;
    @NotBlank(message = "region in S3Properties is blank")
    private String region;
    @NotBlank(message = "bucket in S3Properties is blank")
    private String bucket;
    @NotBlank(message = "accessKey in S3Properties is blank")
    private String accessKey;
    @NotBlank(message = "secretKey in S3Properties is blank")
    private String secretKey;
    @NotBlank(message = "publicBaseUrl in S3Properties is blank")
    private String publicBaseUrl;
    private String coverPrefix = "covers";
    private boolean pathStyleAccess;
}
