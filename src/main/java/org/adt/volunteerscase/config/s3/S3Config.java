package org.adt.volunteerscase.config.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;


@Configuration
@RequiredArgsConstructor
public class S3Config {

    private final S3Properties properties;

    @Bean
    public S3Client s3Client() {
        if (!StringUtils.hasText(properties.getRegion())) {
            throw new IllegalStateException("storage.s3.region must be configured");
        }
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        properties.getAccessKey(),
                                        properties.getSecretKey()
                                )
                        )
                )
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(properties.isPathStyleAccess())
                                .build()
                );


        builder.endpointOverride(endpointUri());

        return builder.build();
    }

    private URI endpointUri() {
        if (!StringUtils.hasText(properties.getEndpoint())) {
            throw new IllegalStateException("storage.s3.endpoint must be configured");
        }

        URI endpoint = URI.create(properties.getEndpoint());
        if (!StringUtils.hasText(endpoint.getScheme())) {
            throw new IllegalStateException("storage.s3.endpoint must include URI scheme, for example https://s3.example.com");
        }

        return endpoint;
    }

}
