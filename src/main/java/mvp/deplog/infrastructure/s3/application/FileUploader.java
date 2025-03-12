package mvp.deplog.infrastructure.s3.application;

import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PartSummary;
import com.amazonaws.services.s3.model.UploadPartResult;
import jakarta.servlet.http.HttpServletRequest;
import mvp.deplog.infrastructure.s3.dto.request.CompleteS3MultipartUploadRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileUploader {

    // 단일 Multipart
    String uploadMultipartFile(MultipartFile file, String dirName) throws IOException;
    // 단일 Octet-Stream
    String uploadStreamFile(HttpServletRequest request, String dirName) throws IOException;
    // 단일 Pre-Signed Url
    String generatePreSignedUrl(String contentType, String dirName);
    // AWS S3 Multipart - 업로드 시작 요청
    InitiateMultipartUploadResult initMultipartUpload(String fileType, String dirName);
    // AWS S3 Multipart - 업로드 완료 요청
    CompleteMultipartUploadResult completeMultipartUpload(String uploadId, List<CompleteS3MultipartUploadRequest.Part> partList, String filePath);
    void abortMultipartUpload(String uploadId, String filePath);
    // AWS S3 Multipart - 파트별 Pre-Signed Url 생성
    String generatePreSignedUrlForMultipartUpload(String uploadId, int partNumber, String filePath);
    void deleteFile(String fileName, String dirName);
    UploadPartResult uploadStreamChunk(String uploadId, int partNumber, String filePath, HttpServletRequest request) throws IOException;
    List<PartSummary> getUploadedPartList(String uploadId, String filePath);
}
