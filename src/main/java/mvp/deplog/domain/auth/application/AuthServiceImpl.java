package mvp.deplog.domain.auth.application;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import mvp.deplog.domain.auth.dto.mapper.MemberAuthMapper;
import mvp.deplog.domain.auth.dto.request.JoinReq;
import mvp.deplog.domain.auth.dto.request.LoginReq;
import mvp.deplog.domain.auth.dto.request.LogoutReq;
import mvp.deplog.domain.auth.dto.request.ModifyPasswordReq;
import mvp.deplog.domain.auth.dto.response.EmailDuplicateCheckRes;
import mvp.deplog.domain.auth.dto.response.LoginRes;
import mvp.deplog.domain.auth.dto.response.ReissueRes;
import mvp.deplog.domain.comment.domain.Comment;
import mvp.deplog.domain.comment.domain.repository.CommentRepository;
import mvp.deplog.domain.likes.domain.Likes;
import mvp.deplog.domain.likes.domain.repository.LikesRepository;
import mvp.deplog.domain.member.domain.Member;
import mvp.deplog.domain.member.domain.repository.MemberRepository;
import mvp.deplog.domain.post.domain.Post;
import mvp.deplog.domain.post.domain.repository.PostRepository;
import mvp.deplog.domain.scrap.domain.Scrap;
import mvp.deplog.domain.scrap.domain.repository.ScrapRepository;
import mvp.deplog.domain.tag.domain.Tag;
import mvp.deplog.domain.tag.domain.repository.TagRepository;
import mvp.deplog.domain.tagging.Tagging;
import mvp.deplog.domain.tagging.repository.TaggingRepository;
import mvp.deplog.global.common.Message;
import mvp.deplog.global.common.SuccessResponse;
import mvp.deplog.global.security.UserDetailsImpl;
import mvp.deplog.global.security.jwt.JwtTokenProvider;
import mvp.deplog.infrastructure.redis.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthServiceImpl implements AuthService{

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenValidityInSeconds;

    private static String RT_PREFIX = "RT_";
    private static String BL_AT_PREFIX = "BL_AT_";

    private final RedisUtil redisUtil;
    private final AuthenticationManager authenticationManager;
    private final MemberAuthMapper memberAuthMapper;

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikesRepository likesRepository;
    private final ScrapRepository scrapRepository;
    private final TaggingRepository taggingRepository;
    private final TagRepository tagRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public SuccessResponse<Message> join(JoinReq joinReq) {
        String email = joinReq.getEmail();
        checkJoinVerify(joinReq.getEmail());

        if (memberRepository.existsByEmail(email))
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");

        Member member = memberAuthMapper.joinToMember(joinReq);
        memberRepository.save(member);

        Message message = Message.builder()
                .message("회원 가입이 완료되었습니다.")
                .build();

        return SuccessResponse.of(message);
    }

    @Override
    public SuccessResponse<LoginRes> login(LoginReq loginReq) {
        String email = loginReq.getEmail();
        String password = loginReq.getPassword();

        // 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken();

        memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일로 유저를 찾을 수 없습니다: " + email));

        redisUtil.setDataExpire(RT_PREFIX + refreshToken, email, refreshTokenValidityInSeconds);

        LoginRes loginRes = LoginRes.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return SuccessResponse.of(loginRes);
    }

    @Override
    public SuccessResponse<Message> logout(UserDetailsImpl userDetails, LogoutReq logoutReq) {
        String email = userDetails.getMember().getEmail();
        String findEmail = redisUtil.getData(RT_PREFIX + logoutReq.getRefreshToken());
        if (!email.equals(findEmail))
            throw new IllegalArgumentException("본인의 리프레시 토큰만 삭제할 수 있습니다.");
        redisUtil.deleteData(RT_PREFIX + logoutReq.getRefreshToken());

        // 남은 시간을 초 단위로 계산
        DecodedJWT decodedJWT = JWT.decode(logoutReq.getAccessToken());
        Instant expiresAt = decodedJWT.getExpiresAt().toInstant();
        Instant now = Instant.now();
        long between = ChronoUnit.SECONDS.between(now, expiresAt);
        System.out.println("남은 시간: " + between);

        // 남은 만료시간만큼 access token blacklist
        redisUtil.setDataExpire(BL_AT_PREFIX + logoutReq.getAccessToken(), "black list token", between);

        Message message = Message.builder()
                .message("로그아웃이 완료되었습니다.")
                .build();

        return SuccessResponse.of(message);
    }

    @Override
    @Transactional
    public SuccessResponse<Message> exit(UserDetailsImpl userDetails) {
        Member member = memberRepository.findById(userDetails.getMember().getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // method : handle likes - delete likes
        List<Likes> likesList = likesRepository.findAllByMember(member);
        for (Likes likes : likesList) {
            Post post = likes.getPost();
            if(post.getMember().equals(member))
                continue;
            post.decrementLikesCount();
        }
        likesRepository.deleteAll(likesList);

        // method : handle scrap - delete scrap
        List<Scrap> scrapList = scrapRepository.findAllByMember(member);
        for (Scrap scrap : scrapList) {
            Post post = scrap.getPost();
            if (post.getMember().equals(member))
                continue;
            post.decrementScrapCount();
        }
        scrapRepository.deleteAll(scrapList);

        // method : handle comment - delete comment
        List<Comment> commentList = commentRepository.findByMember(member);
        for (Comment comment : commentList) {
            if (comment.getParentComment() == null) {
                List<Comment> childCommentList = commentRepository.findByParentComment(comment);
                commentRepository.deleteAll(childCommentList);
            }
        }
        commentRepository.deleteAll(commentList);

        // method : handle tag - control tag && delete post
        List<Post> postList = postRepository.findByMember(member);
        for (Post post : postList) {
            List<Tagging> taggingList = taggingRepository.findByPost(post);
            for (Tagging tagging : taggingList) {
                Tag tag = tagging.getTag();
                Long taggingCount = taggingRepository.countByTag(tag);
                taggingRepository.delete(tagging);
                if(taggingCount == 1)
                    tagRepository.delete(tag);
            }

            List<Comment> postCommentList = commentRepository.findByPost(post);
            for (Comment comment : postCommentList) {
                if (comment.getParentComment() == null) {
                    List<Comment> childCommentList = commentRepository.findByParentComment(comment);
                    commentRepository.deleteAll(childCommentList);
                }
            }
            commentRepository.deleteAll(postCommentList);

            List<Likes> postLikes = likesRepository.findByPost(post);
            likesRepository.deleteAll(postLikes);

            List<Scrap> postScraps = scrapRepository.findByPost(post);
            scrapRepository.deleteAll(postScraps);
        }

        postRepository.deleteAll(postList);

        // delete member
        memberRepository.delete(member);

        Message message = Message.builder()
                .message("회원 탈퇴가 완료되었습니다.")
                .build();

        return SuccessResponse.of(message);
    }

    @Override
    public SuccessResponse<ReissueRes> reissue(String refreshToken) {
        if (!jwtTokenProvider.isTokenValid(refreshToken))
            throw new TokenExpiredException("유효하지 않은 리프레시 토큰입니다.");

        String email = redisUtil.getData(RT_PREFIX + refreshToken);
        String accessToken = jwtTokenProvider.createAccessToken(email);

        ReissueRes reissueRes = ReissueRes.builder()
                .accessToken(accessToken)
                .build();

        return SuccessResponse.of(reissueRes);
    }

    @Override
    public SuccessResponse<EmailDuplicateCheckRes> checkEmailDuplicate(String email) {
        EmailDuplicateCheckRes emailDuplicateCheckRes = EmailDuplicateCheckRes.builder()
                .availability(!memberRepository.existsByEmail(email))
                .build();

        return SuccessResponse.of(emailDuplicateCheckRes);
    }

    @Override
    @Transactional
    public SuccessResponse<Message> modifyPassword(ModifyPasswordReq modifyPasswordReq) {
        String email = modifyPasswordReq.getEmail();
        checkPasswordEmailVerify(email);

        memberRepository.findByEmail(email)
                .ifPresentOrElse(
                        member -> member.updatePassword(passwordEncoder.encode(modifyPasswordReq.getPassword())),
                        () -> {
                            throw new IllegalArgumentException("해당 이메일로 가입된 계정이 존재하지 않습니다.");
                        }
                );

        Message message = Message.builder()
                .message("비밀번호 변경이 완료되었습니다.")
                .build();

        return SuccessResponse.of(message);
    }

    private void checkJoinVerify(String email) {
        String data = redisUtil.getData(email + "_verify");
        if (data == null)
            throw new IllegalArgumentException("인증이 필요한 이메일입니다.");
        redisUtil.deleteData(email + "_verify");
    }

    private void checkPasswordEmailVerify(String email) {
        String data = redisUtil.getData(email + "_verify");
        if (data != null)
            throw new IllegalArgumentException("인증이 필요한 이메일입니다.");
    }
}
