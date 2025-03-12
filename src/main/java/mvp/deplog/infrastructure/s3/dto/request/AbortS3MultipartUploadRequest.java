package mvp.deplog.infrastructure.s3.dto.request;

import lombok.Data;

@Data
public class AbortS3MultipartUploadRequest {
    String uploadId;
    String filePath;
}
