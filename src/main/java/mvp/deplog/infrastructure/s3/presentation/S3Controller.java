package mvp.deplog.infrastructure.s3.presentation;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mvp.deplog.global.common.SuccessResponse;
import mvp.deplog.infrastructure.s3.application.S3Service;
import mvp.deplog.infrastructure.s3.dto.request.CompleteS3MultipartUploadRequest;
import mvp.deplog.infrastructure.s3.dto.request.InitS3MultipartUploadRequest;
import mvp.deplog.infrastructure.s3.dto.response.ETagRes;
import mvp.deplog.infrastructure.s3.dto.response.FileUrlRes;
import mvp.deplog.infrastructure.s3.dto.response.LocationRes;
import mvp.deplog.infrastructure.s3.dto.response.InitUploadRes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/s3")
public class S3Controller {

    private final S3Service s3Service;

    // Description : 단일 파일 업로드
    @PostMapping("/upload-multipart")
    public ResponseEntity<SuccessResponse<FileUrlRes>> uploadMultipartFile(
            @RequestPart(value = "postImage") MultipartFile multipartFile
    ) throws IOException {
        return ResponseEntity.ok(s3Service.uploadFile(multipartFile));
    }

    @PostMapping("/upload-stream")
    public ResponseEntity<SuccessResponse<FileUrlRes>> uploadStreamFile(
            HttpServletRequest request
    ) throws IOException {
        return ResponseEntity.ok(s3Service.uploadStreamFile(request));
    }

    @GetMapping("/pre-signed-url")
    public ResponseEntity<SuccessResponse<FileUrlRes>> getPreSignedUrl(
            @RequestParam(value = "contentType") String contentType
    )  {
        return ResponseEntity.ok(s3Service.getPreSignedUrl(contentType));
    }

    // Description : AWS S3 Multipart Upload
    @PostMapping("/init")
    public ResponseEntity<SuccessResponse<InitUploadRes>> initUpload(
            @RequestBody InitS3MultipartUploadRequest initS3MultipartUploadRequest
            ) {
        return ResponseEntity.ok(s3Service.initMultipartUpload(initS3MultipartUploadRequest));
    }

    @PostMapping("/complete")
    public ResponseEntity<SuccessResponse<LocationRes>> completeUpload(
            @RequestBody CompleteS3MultipartUploadRequest completeS3MultipartUploadRequest
            ) {
        return ResponseEntity.ok(s3Service.completeMultipartUploadRequest(completeS3MultipartUploadRequest));
    }

    @PostMapping("/chunk")
    public ResponseEntity<SuccessResponse<ETagRes>> uploadChunk(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("partNumber") int partNumber,
            @RequestParam(value = "filePath") String filePath,
            HttpServletRequest request
    ) throws IOException {
        return ResponseEntity.ok(s3Service.uploadStreamChunk(uploadId, partNumber, filePath, request));
    }

    @GetMapping("/multipart/pre-signed-url")
    public ResponseEntity<SuccessResponse<FileUrlRes>> getMultipartPreSignedUrl(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("partNumber") int partNumber,
            @RequestParam("filePath") String filePath
            )  {
        return ResponseEntity.ok(s3Service.getMultipartPreSignedUrl(uploadId, partNumber, filePath));
    }
}
