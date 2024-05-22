MinIO 是一种对象存储解决方案，它提供与 Amazon Web Services S3 兼容的 API，并支持所有核心 S3 功能。 MinIO 旨在部署在任何地方 - 公共云或私有云、裸机基础设施、编排环境和边缘基础设施。因此本章中提供了S3与Minio两种实现方式



#### 下载安装文件

```sh
wget https://dl.min.io/server/minio/release/linux-arm64/minio
```
#### 启动程序

```sh
MINIO_ROOT_USER=admin MINIO_ROOT_PASSWORD=123456789 ./minio server /data --console-address ":9001"

-- MINIO_ROOT_USER 指定登录账号
-- MINIO_ROOT_PASSWORD 指定登录密码
```

#### 访问网站

http://127.0.0.1:9001

#### 创建访问令牌和密钥

Access Keys ----> Create Access Key

YA7LgtMcxbEhewB0Kdgk

TyrFMkgwMOxo3qEnC6B3DqoGGGqYkNq2rquRbHcp



#### 创建Springboot项目

##### 导入依赖

```xml
 <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>5.8.16</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.minio</groupId>
            <artifactId>minio</artifactId>
            <version>8.5.10</version>
        </dependency>
        <dependency>
            <groupId>com.amazonaws</groupId>
            <artifactId>aws-java-sdk-s3</artifactId>
            <version>1.12.272</version>
        </dependency>
```

##### 配置yml文件

```yaml
spring:
  application:
    name: spring-boot-minio-demo

upload:
  end-point: http://ip:port
  region: xnj
  access-key: YA7LgtMcxbEhewB0Kdgk
  secret-key: TyrFMkgwMOxo3qEnC6B3DqoGGGqYkNq2rquRbHcp


```

##### 声明配置类

```java
@Data
@Configuration
@ConfigurationProperties(prefix = "upload")
public class UploadProperties {
    /**
     * 对象存储服务的URL
     */
    private String endpoint;

    /**
     * 区域
     */
    private String region;

    /**
     * Access key
     */
    private String accessKey;

    /**
     * Secret key
     */
    private String secretKey;
}
```

##### 自定义MinioClient与S3Client

```java
@Configuration
@RequiredArgsConstructor
public class CustomUploadClient {

    private final UploadProperties uploadProperties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(uploadProperties.getEndpoint())
                .region(uploadProperties.getRegion())
                .credentials(uploadProperties.getAccessKey(),uploadProperties.getSecretKey())
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
```

##### 定义接口

``` java

public interface IMinioTemplate {

    boolean existBucket(String bucketName);

    void createBucket(String bucketName);

    void deleteBucket(String bucketName);

    boolean existFile(String bucketName, String relativePath, String fileName);

    void uploadFile(String bucketName, String relativePath, String fileName, File file);

    void uploadFile(String bucketName, String relativePath, String fileName, InputStream inputStream);

    void uploadFile(String bucketName, String relativePath, String fileName, byte[] content);

    InputStream getFile(String bucketName, String relativePath, String fileName);

    void deleteFile(String bucketName, String relativePath, String fileName);

    String getPresignedObjectUrl(String bucketName, String relativePath, String fileName);
}
```

##### MinioClient实现类

```java
package cn.syy.springbootminiodemo.upload;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author syy
 */
@Service("minio")
@RequiredArgsConstructor
public class MinioTemplate implements IMinioTemplate {

    private final MinioClient minioClient;

    @Override
    @SneakyThrows
    public boolean existBucket(String bucketName) {

        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    @Override
    @SneakyThrows
    public void createBucket(String bucketName) {

        if (!existBucket(bucketName)) {

            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    @Override
    @SneakyThrows
    public void deleteBucket(String bucketName) {

        minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    }

    @Override
    public boolean existFile(String bucketName, String relativePath, String fileName) {

        String filePath = Paths.get(relativePath, fileName).toString();

        try {
            StatObjectResponse response = minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(filePath).build());

            return StringUtils.isNotBlank(response.etag());

        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    @SneakyThrows
    public void uploadFile(String bucketName, String relativePath, String fileName, File file) {

        try (InputStream inputStream = new FileInputStream(file)) {

            uploadFile(bucketName, relativePath, fileName, inputStream);
        }
    }

    @Override
    @SneakyThrows
    public void uploadFile(String bucketName, String relativePath, String fileName, InputStream inputStream) {

        String filePath = Paths.get(relativePath, fileName).toString();

        try (inputStream) {
            PutObjectArgs uploadObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .stream(inputStream, inputStream.available(), -1)
                    .object(filePath)
                    .build();

            minioClient.putObject(uploadObjectArgs);
        }
    }

    @Override
    @SneakyThrows
    public void uploadFile(String bucketName, String relativePath, String fileName, byte[] content) {

        try (InputStream inputStream = new ByteArrayInputStream(content)) {

            uploadFile(bucketName, relativePath, fileName, inputStream);
        }
    }

    @Override
    @SneakyThrows
    public InputStream getFile(String bucketName, String relativePath, String fileName) {

        String filePath = Paths.get(relativePath, fileName).toString();

        return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(filePath).build());
    }

    @Override
    @SneakyThrows
    public void deleteFile(String bucketName, String relativePath, String fileName) {

        String filePath = Paths.get(relativePath, fileName).toString();

        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(filePath).build());
    }

    @Override
    @SneakyThrows
    public String getPresignedObjectUrl(String bucketName, String relativePath, String fileName) {

        String filePath = Paths.get(relativePath, fileName).toString();

        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucketName).object(filePath).method(Method.GET).expiry(1, TimeUnit.MINUTES).build());
    }
}

```

##### S3Client实现类

```java
package cn.syy.springbootminiodemo.upload;

import cn.hutool.core.date.DateUtil;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author syy
 */
@Service("s3")
@RequiredArgsConstructor
public class S3Template implements IMinioTemplate {

    private final AmazonS3 amazonS3;

    @Override
    @SneakyThrows
    public boolean existBucket(String bucketName) {

        return amazonS3.doesBucketExistV2(bucketName);
    }

    @Override
    @SneakyThrows
    public void createBucket(String bucketName) {

        if (!existBucket(bucketName)) {

            amazonS3.createBucket(bucketName);
        }
    }

    @Override
    @SneakyThrows
    public void deleteBucket(String bucketName) {

        amazonS3.deleteBucket(bucketName);
    }

    @Override
    public boolean existFile(String bucketName, String relativePath, String fileName) {

        String filePath = Paths.get(relativePath, fileName).toString();

        try {
            return amazonS3.doesObjectExist(bucketName, filePath);
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    @SneakyThrows
    public void uploadFile(String bucketName, String relativePath, String fileName, File file) {

        try (InputStream inputStream = new FileInputStream(file)) {

            uploadFile(bucketName, relativePath, fileName, inputStream);
        }
    }

    @Override
    @SneakyThrows
    public void uploadFile(String bucketName, String relativePath, String fileName, InputStream inputStream) {

        String filePath = Paths.get(relativePath, fileName).toString();

        try (inputStream) {

            amazonS3.putObject(bucketName, filePath, inputStream, null);
        }
    }

    @Override
    @SneakyThrows
    public void uploadFile(String bucketName, String relativePath, String fileName, byte[] content) {

        try (InputStream inputStream = new ByteArrayInputStream(content)) {

            uploadFile(bucketName, relativePath, fileName, inputStream);
        }
    }

    @Override
    @SneakyThrows
    public InputStream getFile(String bucketName, String relativePath, String fileName) {

        String filePath = Paths.get(relativePath, fileName).toString();

        S3Object object = amazonS3.getObject(bucketName, filePath);

        return object.getObjectContent();
    }

    @Override
    @SneakyThrows
    public void deleteFile(String bucketName, String relativePath, String fileName) {

        String filePath = Paths.get(relativePath, fileName).toString();

        amazonS3.deleteObject(bucketName, filePath);
    }

    @Override
    @SneakyThrows
    public String getPresignedObjectUrl(String bucketName, String relativePath, String fileName) {

        String filePath = Paths.get(relativePath, fileName).toString();

        Date date = DateUtils.addMinutes(new Date(), 1);

        return amazonS3.generatePresignedUrl(bucketName, filePath, date, HttpMethod.GET).toString();
    }
}

```

