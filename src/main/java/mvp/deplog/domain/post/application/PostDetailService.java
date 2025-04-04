package mvp.deplog.domain.post.application;

import mvp.deplog.domain.post.dto.PostDetailParams;
import mvp.deplog.global.common.SuccessResponse;
import mvp.deplog.global.security.UserDetailsImpl;

public interface PostDetailService<T> {

    boolean supports(UserDetailsImpl userDetails);

    SuccessResponse<T> getPostDetail(PostDetailParams postDetailParams);
}
