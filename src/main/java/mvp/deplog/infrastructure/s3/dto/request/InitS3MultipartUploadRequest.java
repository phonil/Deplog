package mvp.deplog.infrastructure.s3.dto.request;

import lombok.Data;

@Data
public class InitS3MultipartUploadRequest {
    String fileType;
}
