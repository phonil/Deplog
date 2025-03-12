package mvp.deplog.infrastructure.s3.application;

import com.amazonaws.services.s3.model.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mvp.deplog.global.common.SuccessResponse;
import mvp.deplog.infrastructure.s3.dto.request.AbortS3MultipartUploadRequest;
import mvp.deplog.infrastructure.s3.dto.request.CompleteS3MultipartUploadRequest;
import mvp.deplog.infrastructure.s3.dto.request.InitS3MultipartUploadRequest;
import mvp.deplog.infrastructure.s3.dto.request.PreResumePartRequest;
import mvp.deplog.infrastructure.s3.dto.response.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
    public SuccessResponse<InitUploadRes> initMultipartUpload(InitS3MultipartUploadRequest initMultipartUploadRequest) {
        InitiateMultipartUploadResult initiateMultipartUploadResult = fileUploader.initMultipartUpload(
                initMultipartUploadRequest.getFileType(),
                DIRNAME
        );
        InitUploadRes initUploadRes = InitUploadRes.builder()
                .uploadId(initiateMultipartUploadResult.getUploadId())
                .filePath(initiateMultipartUploadResult.getKey())
                .build();
        return SuccessResponse.of(initUploadRes);
    }

    public SuccessResponse<LocationRes> completeMultipartUpload(CompleteS3MultipartUploadRequest completeS3MultipartUploadRequest) {
        CompleteMultipartUploadResult completeMultipartUploadResult =
                fileUploader.completeMultipartUpload(
                        completeS3MultipartUploadRequest.getUploadId(),
                        completeS3MultipartUploadRequest.getPartList(),
                        completeS3MultipartUploadRequest.getFilePath()
                );
        LocationRes locationRes = LocationRes.builder()
                .location(completeMultipartUploadResult.getLocation())
                .build();
        return SuccessResponse.of(locationRes);
    }

    public void abortMultipartUpload(AbortS3MultipartUploadRequest abortS3MultipartUploadRequest) {
        fileUploader.abortMultipartUpload(
                abortS3MultipartUploadRequest.getUploadId(),
                abortS3MultipartUploadRequest.getFilePath()
        );
    }

    public SuccessResponse<ETagRes> uploadStreamChunk(String uploadId, int partNumber, String filePath, HttpServletRequest request) throws IOException {
        UploadPartResult uploadPartResult = fileUploader.uploadStreamChunk(uploadId, partNumber, filePath, request);
        ETagRes eTagRes = ETagRes.builder()
                .eTag(uploadPartResult.getETag())
                .build();
        return SuccessResponse.of(eTagRes);
    }

    public SuccessResponse<FileUrlRes> getMultipartPreSignedUrl(String uploadId, int partNumber, String filePath) {
        String preSignedUrl = fileUploader.generatePreSignedUrlForMultipartUpload(uploadId, partNumber, filePath);
        FileUrlRes fileUrlRes = FileUrlRes.builder()
                .fileUrl(preSignedUrl)
                .build();
        return SuccessResponse.of(fileUrlRes);
    }

    public SuccessResponse<UploadedPartResponse> getPreResumeChunk(PreResumePartRequest preResumePartRequest) {
        List<PartSummary> uploadedPartSummaryList = fileUploader.getUploadedPartList(
                preResumePartRequest.getUploadId(),
                preResumePartRequest.getFilePath()
        );
        List<UploadedPartResponse.Part> uploadedPartList = uploadedPartSummaryList.stream()
                .map(partSummary ->
                        UploadedPartResponse.Part.builder()
                                .partNumber(partSummary.getPartNumber())
                                .eTag(partSummary.getETag())
                                .build()
                ).toList();
        UploadedPartResponse uploadedPartResponse = UploadedPartResponse.builder()
                .uploadedPartList(uploadedPartList)
                .build();
        return SuccessResponse.of(uploadedPartResponse);
    }
}
