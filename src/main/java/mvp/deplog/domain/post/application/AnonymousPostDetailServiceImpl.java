package mvp.deplog.domain.post.application;

import lombok.RequiredArgsConstructor;
import mvp.deplog.domain.member.WriterInfo;
import mvp.deplog.domain.member.domain.Member;
import mvp.deplog.domain.member.domain.Role;
import mvp.deplog.domain.member.dto.Avatar;
import mvp.deplog.domain.post.domain.Post;
import mvp.deplog.domain.post.domain.Stage;
import mvp.deplog.domain.post.domain.repository.PostRepository;
import mvp.deplog.domain.post.dto.response.AnonymousPostDetailRes;
import mvp.deplog.domain.post.exception.ResourceNotFoundException;
import mvp.deplog.domain.tagging.repository.TaggingRepository;
import mvp.deplog.global.common.SuccessResponse;
import mvp.deplog.global.security.UserDetailsImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
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
    public SuccessResponse<AnonymousPostDetailRes> getPostDetail(UserDetailsImpl userDetails, Long postId) {
        Post post = postRepository.findByIdAndStage(postId, Stage.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("해당 id의 게시글을 찾을 수 없습니다: " + postId));

        // Tagging Entity에 Tag 목록 조회
        List<String> tagNameList = taggingRepository.findByPost(post).stream()
                .map(tagging -> tagging.getTag().getName())
                .collect(Collectors.toList());

        post.incrementViewCount();  // 조회수 증가

        Member writer = post.getMember();

        AnonymousPostDetailRes anonymousPostDetailRes = AnonymousPostDetailRes.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .createdDate(post.getCreatedDate().toLocalDate())
                .content(post.getContent())
                .tagNameList(tagNameList)
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .scrapCount(post.getScrapCount())
                .writerInfo(WriterInfo.builder()
                        .avatar(Avatar.builder()
                                .avatarFace(writer.getAvatarFace())
                                .avatarBody(writer.getAvatarBody())
                                .avatarEyes(writer.getAvatarEyes())
                                .avatarNose(writer.getAvatarNose())
                                .avatarMouth(writer.getAvatarMouth())
                                .build()
                        )
                        .name(writer.getName())
                        .generation(writer.getGeneration())
                        .part(writer.getPart())
                        .build()
                )
                .build();

        return SuccessResponse.of(anonymousPostDetailRes);
    }
}
