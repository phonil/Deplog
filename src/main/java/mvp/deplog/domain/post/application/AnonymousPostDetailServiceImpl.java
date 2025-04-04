package mvp.deplog.domain.post.application;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import mvp.deplog.domain.member.domain.Member;
import mvp.deplog.domain.member.domain.Role;
import mvp.deplog.domain.post.domain.Post;
import mvp.deplog.domain.post.domain.Stage;
import mvp.deplog.domain.post.domain.repository.PostRepository;
import mvp.deplog.domain.post.dto.PostDetailParams;
import mvp.deplog.domain.post.dto.response.AnonymousPostDetailRes;
import mvp.deplog.domain.post.exception.ResourceNotFoundException;
import mvp.deplog.domain.tagging.repository.TaggingRepository;
import mvp.deplog.global.common.SuccessResponse;
import mvp.deplog.global.security.UserDetailsImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;

import java.util.List;
import java.util.stream.Collectors;

import static mvp.deplog.domain.post.PostConstant.AnonymousPostDetailConstant.*;

@RequiredArgsConstructor
@Service
public class AnonymousPostDetailServiceImpl implements PostDetailService<AnonymousPostDetailRes> {

    private final PostRepository postRepository;
    private final TaggingRepository taggingRepository;

    @Override
    public boolean supports(UserDetailsImpl userDetails) {
        return userDetails == null || userDetails.getMember().getRole().equals(Role.APPLICANT);
    }

    @Override
    @Transactional
    public SuccessResponse<AnonymousPostDetailRes> getPostDetail(PostDetailParams postDetailParams) {
        Post post = postRepository.findByIdAndStage(postDetailParams.getPostId(), Stage.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("해당 id의 게시글을 찾을 수 없습니다: "));

        String cookieName = POST_COOKIE_NAME_PREFIX + postDetailParams.getPostId();
        Cookie existingCookie = WebUtils.getCookie(postDetailParams.getRequest(), cookieName);
        if (existingCookie == null) {
            post.incrementViewCount();
            Cookie newCookie = new Cookie(cookieName, POST_COOKIE_VALUE);
            newCookie.setPath(COOKIE_PATH);
            newCookie.setMaxAge(COOKIE_AGE);
            newCookie.setHttpOnly(true);
            postDetailParams.getResponse().addCookie(newCookie);
        }

        List<String> tagNameList = taggingRepository.findByPost(post).stream()
                .map(tagging -> tagging.getTag().getName())
                .collect(Collectors.toList());

        Member writer = post.getMember();
        AnonymousPostDetailRes anonymousPostDetailRes = AnonymousPostDetailRes.of(post, writer, tagNameList);
        return SuccessResponse.of(anonymousPostDetailRes);
    }
}
