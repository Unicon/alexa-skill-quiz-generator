package org.unicon.lex.services.external;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class CanvasServiceImpl implements CanvasService {
    private static final String FILE_UPLOAD_URL = "canvas.api.fileUpload.url";
    private static final String FOLDER_URL = "canvas.api.openstax.folder.url";
    private static final String TOKEN = "canvas.api.token";

    private final Logger log = LogManager.getLogger(getClass());

    private final Properties properties;

    private String retrieveUrl;
    private String folderUrl;
    private String apiToken;
    private ObjectMapper mapper = new ObjectMapper();

    public CanvasServiceImpl(Properties properties) {
        this.properties = properties;
        this.retrieveUrl = properties.getProperty(FILE_UPLOAD_URL);
        this.folderUrl = properties.getProperty(FOLDER_URL);
        this.apiToken = properties.getProperty(TOKEN);
    }

    public File downloadFileFromCanvas(String fileName) {
        String downloadUrl = getDownloadUrl(fileName);
        return getFileFromCanvas(downloadUrl, fileName);
    }

    public List<String> getFilesFromFolder() {
        List<String> files = new ArrayList<>();
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet filesGet = new HttpGet(folderUrl);
        filesGet.addHeader("Authorization", "Bearer " + apiToken);
        filesGet.setHeader("Accept", "application/json");

        try {
            CloseableHttpResponse response = client.execute(filesGet);
            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8.name());
            log.debug(responseBody);
            List<Map<String, String>> retrievedList = mapper.readValue(responseBody, new TypeReference<List<Map<String, String>>>(){});
            for (Map<String, String> map : retrievedList) {
                files.add(map.get("filename"));
            }
            log.debug("Number of files: " + files.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return files;
    }

    private File getFileFromCanvas(String downloadUrl, String fileName) {
        File file = new File("/tmp/" + fileName);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet downloadGet = new HttpGet(downloadUrl);
        downloadGet.setHeader("Accept", "application/pdf");
        log.debug(file.getTotalSpace());

        try {
            CloseableHttpResponse response = client.execute(downloadGet);

            file.createNewFile();
            BufferedInputStream bis = new BufferedInputStream(response.getEntity().getContent());
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            int inByte;
            while((inByte = bis.read()) != -1) {
                bos.write(inByte);
            }
            bis.close();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.debug(file.getTotalSpace());
        return file;
    }

    private String getDownloadUrl(String fileName) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet downloadUrlGet = new HttpGet(retrieveUrl);
        downloadUrlGet.addHeader("Authorization", "Bearer " + apiToken);
        downloadUrlGet.setHeader("Accept", "application/json");
        String downloadUrl = "";

        try {
            CloseableHttpResponse response = client.execute(downloadUrlGet);
            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8.name());
            log.debug(responseBody);
            List<Map<String, String>> retrievedList = mapper.readValue(responseBody, new TypeReference<List<Map<String, String>>>(){});
            for (Map<String, String> map : retrievedList) {
                String retrievedFileName = map.get("display_name");
                if (StringUtils.equalsIgnoreCase(retrievedFileName, fileName)) {
                    downloadUrl = map.get("url");
                    log.debug(downloadUrl);
                    break;
                }
            }
        } catch (Exception e) {
            log.error(e.getStackTrace());
        }
        return downloadUrl;
    }

    public void uploadFileToCanvas(String fileName, MultipartFile file) {
        String uploadUrl = postForCanvasFileUploadUrl(fileName);
        postToUploadFile(uploadUrl, fileName, file);
    }

    private String postForCanvasFileUploadUrl(String fileName) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost retrievePost = new HttpPost(retrieveUrl);
        retrievePost.addHeader("Authorization", "Bearer " + apiToken);
        retrievePost.setHeader("Accept", "application/json");

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("name", fileName));
        String uploadUrl = "";

        try {
            retrievePost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            CloseableHttpResponse response = client.execute(retrievePost);
            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8.name());
            Map<String, String> retrieveMap = mapper.readValue(responseBody, Map.class);
            uploadUrl = retrieveMap.get("upload_url");
            log.debug(uploadUrl);


        } catch (Exception e) {
            log.error(e.getStackTrace());
        }

        return uploadUrl;
    }

    private void postToUploadFile(String uploadUrl, String fileName, MultipartFile file) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost uploadPost = new HttpPost(uploadUrl);
        uploadPost.addHeader("Authorization", "Bearer " + apiToken);
        uploadPost.setHeader("Accept", "application/json");

        try {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody( "file", file.getInputStream(), ContentType.APPLICATION_OCTET_STREAM, fileName);
            HttpEntity fileBody = builder.build();
            uploadPost.setEntity(fileBody);

            CloseableHttpResponse response = client.execute(uploadPost);
            String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8.name());
            log.debug(responseBody);
        } catch (Exception e) {
            log.error(e.getStackTrace());
        }
    }

}
