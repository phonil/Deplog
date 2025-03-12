package mvp.deplog.infrastructure.s3.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ETagRes {
    String eTag;
}
