package mvp.deplog.domain.post.dto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.Data;
import mvp.deplog.global.security.UserDetailsImpl;

@Data
@Builder
public class PostDetailParams {
    private UserDetailsImpl userDetails;
    private Long postId;
    private HttpServletRequest request;
    private HttpServletResponse response;

    public static PostDetailParams of(UserDetailsImpl userDetails, Long postId, HttpServletRequest request, HttpServletResponse response) {
        return PostDetailParams.builder()
                .userDetails(userDetails)
                .postId(postId)
                .request(request)
                .response(response)
                .build();
    }
}
