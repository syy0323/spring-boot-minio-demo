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
