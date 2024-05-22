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
