package org.unicon.lex.services.external;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

    /**
     * reads the QLearner course model (string-encoded JSON object from S3
     * @param courseName
     * @return
     */
    String getCourseModel(String courseName);

    /**
     * Uploads file to quiz directory in s3 bucket
     * @param file
     */
    void uploadExam(MultipartFile file);

    /**
     * Uploads file to quiz directory in s3 bucket
     * @param file
     */
    void uploadCSVTestBank(MultipartFile file);

    /**
     * Uploads file to glossary directory in s3 bucket
     * @param file
     */
    void uploadGlossary(MultipartFile file);
}
