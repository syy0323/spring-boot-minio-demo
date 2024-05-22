package cn.syy.springbootminiodemo.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author syy
 */
@Configuration
@RequiredArgsConstructor
public class CustomUploadClient {

    private final UploadProperties uploadProperties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(uploadProperties.getEndpoint())
                .region(uploadProperties.getRegion())
                .credentials(uploadProperties.getAccessKey(), uploadProperties.getSecretKey())
                .build();
    }

    @Bean
    public AmazonS3 amazonS3Client() {

        return AmazonS3Client.builder()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(uploadProperties.getEndpoint(), uploadProperties.getRegion()))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(uploadProperties.getAccessKey(), uploadProperties.getSecretKey())))
                .build();
    }
}
