package org.adt.volunteerscase.config.s3;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "storage.s3")
public class S3Properties {
    private String endpoint;
    private String region;
    private String bucket;
    private String accessKey;
    private String secretKey;
    private String publicBaseUrl;
    private String coverPrefix = "covers";
    private boolean pathStyleAccess;
}
