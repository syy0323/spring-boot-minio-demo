package cn.syy.springbootminiodemo.upload;

import io.minio.messages.Bucket;

import java.io.File;
import java.io.InputStream;
import java.util.List;

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
