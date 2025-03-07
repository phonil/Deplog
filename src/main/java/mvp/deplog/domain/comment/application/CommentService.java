package mvp.deplog.domain.comment.application;

import lombok.RequiredArgsConstructor;
import mvp.deplog.domain.comment.domain.Comment;
import mvp.deplog.domain.comment.domain.repository.CommentRepository;
import mvp.deplog.domain.comment.dto.response.CommentListRes;
import mvp.deplog.domain.comment.dto.response.ReplyListRes;
import mvp.deplog.domain.member.domain.Member;
import mvp.deplog.domain.member.dto.Avatar;
import mvp.deplog.domain.post.domain.Post;
import mvp.deplog.domain.post.domain.repository.PostRepository;
import mvp.deplog.global.common.SuccessResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public SuccessResponse<List<CommentListRes>> getCommentList(Long postId){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 아이디의 게시글이 없습니다: " + postId));

        List<Comment> commentList = commentRepository.findByPostAndParentCommentIsNull(post);
        List<Comment> replyList = commentRepository.findByPostAndParentCommentIsNotNull(post);

        List<CommentListRes> commentDetails = new ArrayList<>();

        for(Comment comment : commentList) {
            Avatar avatar = commentAvatar(comment);

            CommentListRes commentListRes = CommentListRes.builder()
                    .commentId(comment.getId())
                    .avatar(avatar)
                    .nickname(comment.getNickname())
                    .createdDate(comment.getCreatedDate().toLocalDate())
                    .content(comment.getContent())
                    .replyList(new ArrayList<>())   // 대댓글 리스트 초기화
                    .build();

            commentDetails.add(commentListRes);
        }

        for(Comment reply : replyList) {
            Avatar avatar = commentAvatar(reply);

            ReplyListRes replyListRes = ReplyListRes.builder()
                    .commentId(reply.getId())
                    .parentCommentId(reply.getParentComment().getId())
                    .avatar(avatar)
                    .nickname(reply.getNickname())
                    .createdDate(reply.getCreatedDate().toLocalDate())
                    .content(reply.getContent())
                    .build();

            commentDetails.stream()
                    .filter(parentComment -> parentComment.getCommentId().equals(reply.getParentComment().getId()))
                    .findFirst()
                    .ifPresent(parentComment -> parentComment.getReplyList().add(replyListRes));
        }

        return SuccessResponse.of(commentDetails);
    }

    private Avatar commentAvatar(Comment comment){
        if(!comment.isUseNickname()) {
            Member member = comment.getMember();
            return Avatar.builder()
                    .avatarFace(member.getAvatarFace())
                    .avatarBody(member.getAvatarBody())
                    .avatarEyes(member.getAvatarEyes())
                    .avatarNose(member.getAvatarNose())
                    .avatarMouth(member.getAvatarMouth())
                    .build();
        }
        return Avatar.builder()
                .avatarFace(null)
                .avatarBody(null)
                .avatarEyes(null)
                .avatarNose(null)
                .avatarMouth(null)
                .build();
    }
}
