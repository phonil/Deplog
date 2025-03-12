package mvp.deplog.infrastructure.s3.application;

import com.amazonaws.services.s3.model.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mvp.deplog.global.common.SuccessResponse;
import mvp.deplog.infrastructure.s3.dto.request.CompleteS3MultipartUploadRequest;
import mvp.deplog.infrastructure.s3.dto.request.InitS3MultipartUploadRequest;
import mvp.deplog.infrastructure.s3.dto.response.ETagRes;
import mvp.deplog.infrastructure.s3.dto.response.FileUrlRes;
import mvp.deplog.infrastructure.s3.dto.response.LocationRes;
import mvp.deplog.infrastructure.s3.dto.response.InitUploadRes;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static mvp.deplog.infrastructure.s3.S3Constant.DIRNAME;


@RequiredArgsConstructor
@Service
public class S3Service {

    private final FileUploader fileUploader;

    public SuccessResponse<FileUrlRes> uploadFile(MultipartFile multipartFile) throws IOException {
        String filePath = fileUploader.uploadMultipartFile(multipartFile, DIRNAME);
        FileUrlRes fileUrlRes = FileUrlRes.builder()
                .fileUrl(filePath)
                .build();
        return SuccessResponse.of(fileUrlRes);
    }

    public SuccessResponse<FileUrlRes> uploadStreamFile(HttpServletRequest request) throws IOException{
        String filePath = fileUploader.uploadStreamFile(request, DIRNAME);
        FileUrlRes fileUrlRes = FileUrlRes.builder()
                .fileUrl(filePath)
                .build();
        return SuccessResponse.of(fileUrlRes);
    }

    public SuccessResponse<FileUrlRes> getPreSignedUrl(String contentType) {
        String preSignedUrl = fileUploader.generatePreSignedUrl(contentType, DIRNAME);
        FileUrlRes fileUrlRes = FileUrlRes.builder()
                .fileUrl(preSignedUrl)
                .build();
        return SuccessResponse.of(fileUrlRes);
    }

    // Description : AWS Multipart Upload
    public SuccessResponse<InitUploadRes> initMultipartUpload(InitS3MultipartUploadRequest initiateMultipartUploadRequest) {
        InitiateMultipartUploadResult initiateMultipartUploadResult = fileUploader.initMultipartUpload(
                initiateMultipartUploadRequest.getFileType(),
                DIRNAME
        );
        InitUploadRes initUploadRes = InitUploadRes.builder()
                .uploadId(initiateMultipartUploadResult.getUploadId())
                .filePath(initiateMultipartUploadResult.getKey())
                .build();
        return SuccessResponse.of(initUploadRes);
    }

    public SuccessResponse<LocationRes> completeMultipartUploadRequest(CompleteS3MultipartUploadRequest completeS3MultipartUploadRequest) {
        CompleteMultipartUploadResult completeMultipartUploadResult =
                fileUploader.completeMultipartUpload(
                        completeS3MultipartUploadRequest.getUploadId(),
                        completeS3MultipartUploadRequest.getPartList(),
                        completeS3MultipartUploadRequest.getFilePath(),
                        DIRNAME
                );
        LocationRes locationRes = LocationRes.builder()
                .location(completeMultipartUploadResult.getLocation())
                .build();
        return SuccessResponse.of(locationRes);
    }

    public SuccessResponse<ETagRes> uploadStreamChunk(String uploadId, int partNumber, String filePath, HttpServletRequest request) throws IOException {
        UploadPartResult uploadPartResult = fileUploader.uploadStreamChunk(uploadId, partNumber, filePath, request);
        ETagRes eTagRes = ETagRes.builder()
                .eTag(uploadPartResult.getETag())
                .build();
        return SuccessResponse.of(eTagRes);
    }

    public SuccessResponse<FileUrlRes> getMultipartPreSignedUrl(String uploadId, int partNumber, String filePath) {
        String preSignedUrl = fileUploader.generatePreSignedUrlForMultipartUpload(uploadId, partNumber, filePath, DIRNAME);
        FileUrlRes fileUrlRes = FileUrlRes.builder()
                .fileUrl(preSignedUrl)
                .build();
        return SuccessResponse.of(fileUrlRes);
    }
}
