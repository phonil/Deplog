package mvp.deplog.domain.post.application;

import lombok.RequiredArgsConstructor;
import mvp.deplog.domain.likes.domain.repository.LikesRepository;
import mvp.deplog.domain.member.domain.Member;
import mvp.deplog.domain.member.domain.Role;
import mvp.deplog.domain.member.domain.repository.MemberRepository;
import mvp.deplog.domain.post.domain.Post;
import mvp.deplog.domain.post.domain.Stage;
import mvp.deplog.domain.post.domain.repository.PostRepository;
import mvp.deplog.domain.post.dto.PostDetailParams;
import mvp.deplog.domain.post.dto.response.MemberPostDetailRes;
import mvp.deplog.domain.post.exception.ResourceNotFoundException;
import mvp.deplog.domain.scrap.domain.repository.ScrapRepository;
import mvp.deplog.domain.tagging.repository.TaggingRepository;
import mvp.deplog.global.common.SuccessResponse;
import mvp.deplog.global.security.UserDetailsImpl;
import mvp.deplog.infrastructure.redis.RedisUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static mvp.deplog.domain.post.PostConstant.MemberPostDetailConstant.*;

@RequiredArgsConstructor
@Service
public class MemberPostDetailServiceImpl implements PostDetailService<MemberPostDetailRes> {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final TaggingRepository taggingRepository;
    private final LikesRepository likesRepository;
    private final ScrapRepository scrapRepository;
    private final RedisUtil redisUtil;

    @Override
    public boolean supports(UserDetailsImpl userDetails) {
        return userDetails != null && (userDetails.getMember().getRole().equals(Role.MEMBER) || userDetails.getMember().getRole().equals(Role.ADMIN));
    }

    @Override
    @Transactional
    public SuccessResponse<MemberPostDetailRes> getPostDetail(PostDetailParams postDetailParams) {
        Member member = postDetailParams.getUserDetails().getMember();
        Post post = postRepository.findByIdAndStage(postDetailParams.getPostId(), Stage.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("해당 id의 게시글을 찾을 수 없습니다"));

        String key = REDIS_KEY_MEMBER_PREFIX + member.getId() + REDIS_KEY_POST_PREFIX + post.getId();
        if (!redisUtil.hasKey(key)) {
            postRepository.incrementViewCount(postDetailParams.getPostId());
            redisUtil.setDataExpire(key, POST_REDIS_VALUE, REDIS_DURATION);
        }
        List<String> tagNameList = taggingRepository.findByPost(post).stream()
                .map(tagging -> tagging.getTag().getName())
                .collect(Collectors.toList());

        Member writer = post.getMember();
        boolean sameUser = member.equals(writer);
        boolean liked = likesRepository.existsByMemberAndPost(member, post);
        boolean scraped = scrapRepository.existsByMemberAndPost(member, post);

        MemberPostDetailRes memberPostDetailRes = MemberPostDetailRes.of(post, writer, tagNameList, sameUser, liked, scraped);
        return SuccessResponse.of(memberPostDetailRes);
    }
}
