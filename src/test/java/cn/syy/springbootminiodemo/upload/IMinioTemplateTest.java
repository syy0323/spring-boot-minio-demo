package cn.syy.springbootminiodemo.upload;

import cn.hutool.core.io.FileUtil;
import cn.syy.springbootminiodemo.SpringBootMinioDemoApplication;
import io.minio.messages.Bucket;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootMinioDemoApplication.class)
class IMinioTemplateTest {

    @Autowired
    @Qualifier("s3")
    private IMinioTemplate uploadTemplate;

    private final String BUCKET_NAME = "bucket-cs";

    private final String RELATIVE_PATH = "\\cs";

    @Test
    void existBucket() {

        System.out.println(uploadTemplate.existBucket(BUCKET_NAME));
    }

    @Test
    void createBucket() {

        uploadTemplate.createBucket(BUCKET_NAME);
    }

    @Test
    void deleteBucket() {

        uploadTemplate.deleteBucket("test");
    }

    @Test
    void existFile() {

        boolean existFile = uploadTemplate.existFile(BUCKET_NAME, RELATIVE_PATH, "02.txt");

        System.out.println(existFile);
    }

    @Test
    void uploadFile() {

        uploadTemplate.uploadFile(BUCKET_NAME, RELATIVE_PATH, "02.txt", new File("D:\\02.txt"));
    }

    @Test
    void getFile() {

        InputStream inputStream = uploadTemplate.getFile(BUCKET_NAME, RELATIVE_PATH, "01.txt");

        FileUtil.writeFromStream(inputStream, new File("D:\\02.txt"));
    }

    @Test
    void deleteFile() {

        uploadTemplate.deleteFile(BUCKET_NAME, RELATIVE_PATH, "02.txt");
    }

    @Test
    void getPresignedObjectUrl() {

        String presignedObjectUrl = uploadTemplate.getPresignedObjectUrl(BUCKET_NAME, RELATIVE_PATH, "02.txt");

        System.out.println(presignedObjectUrl);
    }
}