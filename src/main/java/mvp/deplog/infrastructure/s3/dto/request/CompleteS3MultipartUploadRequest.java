package mvp.deplog.infrastructure.s3.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CompleteS3MultipartUploadRequest {
    String uploadId;
    List<Part> partList;
    String filePath;

    @Data
    public class Part {
        int partNumber;
        String eTag;
    }
}
