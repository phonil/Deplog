package mvp.deplog.infrastructure.s3.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UploadedPartResponse {

    List<Part> uploadedPartList;

    @Data
    @Builder
    public static class Part {
        int partNumber;
        String eTag;
    }
}
