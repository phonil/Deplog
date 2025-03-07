package mvp.deplog.infrastructure.s3.application;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileUploader {

    String uploadMultipartFile(MultipartFile file, String dirName) throws IOException;
    String uploadStreamFile(HttpServletRequest request, String dirName) throws IOException;
    String generatePreSignedUrl(String contentType, String dirName);
    void deleteFile(String fileName, String dirName);
}
