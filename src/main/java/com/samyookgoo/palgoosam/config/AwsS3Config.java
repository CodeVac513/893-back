package com.samyookgoo.palgoosam.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsS3Config {
    // TODO: 배포 후 삭제
    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    // TODO: 배포 후 삭제
    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.s3.endpoint-url}")
    private String s3EndpointUrl;

    @Bean
    public S3Client amazonS3() {
        // TODO: 배포 후 삭제
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(s3EndpointUrl))
                //.credentialsProvider(DefaultCredentialsProvider.create()) // TODO: 배포 후 적용 (IAM 역할을 통한 자격 증명 자동 처리)
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials)) // TODO: 배포 후 삭제
                .build();
    }
}
