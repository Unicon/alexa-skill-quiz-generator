package org.unicon.lex.services.external;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface CanvasService {

    void uploadFileToCanvas(String fileName, MultipartFile file);

    File downloadFileFromCanvas(String fileName);

    List<String> getFilesFromFolder();
}
