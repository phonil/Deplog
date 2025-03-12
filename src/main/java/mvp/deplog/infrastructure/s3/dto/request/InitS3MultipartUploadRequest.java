package mvp.deplog.infrastructure.s3.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InitS3MultipartUploadRequest {
    String fileType;
}
