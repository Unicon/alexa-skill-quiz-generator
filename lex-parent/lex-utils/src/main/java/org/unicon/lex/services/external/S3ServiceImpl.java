package org.unicon.lex.services.external;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;
import org.unicon.lex.AwsFactory;

import java.io.IOException;
import java.util.Properties;

public class S3ServiceImpl implements S3Service {
    private final Logger log = LogManager.getLogger(getClass());
    private AmazonS3 client;

    public S3ServiceImpl(Properties properties) {
        AwsFactory awsFactory = new AwsFactory(properties);
        client = awsFactory.getS3Client();
    }

    public String getCourseModel(String courseName) {
        String bucketName = "mdg-lex-east";
        String path = "data/rl-model/" + courseName + ".json";
        String json = client.getObjectAsString(bucketName, path);
        log.error("retrieved model [{}]", json);
        return json;
    }

    public void uploadExam(MultipartFile file) {
        String bucketName = "mdg-lex-east";
        String path = "quiz/" + file.getName();
        uploadToS3(bucketName, path, file);
    }
    
    public void uploadCSVTestBank(MultipartFile file) {
        String bucketName = "mdg-lex-east";
        String path = "data/quiz/" + file.getName();
        uploadToS3(bucketName, path, file);
    }

    public void uploadGlossary(MultipartFile file) {
        String bucketName = "mdg-lex-east";
        String path = "data/glossary/" + file.getName();
        uploadToS3(bucketName, path, file);
    }
    
    private void uploadToS3(String bucketName, String path, MultipartFile file) {
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            client.putObject(bucketName, path, file.getInputStream(), objectMetadata);
        } catch (IOException e) {
            log.error(e.getStackTrace());
        }
    }
}
