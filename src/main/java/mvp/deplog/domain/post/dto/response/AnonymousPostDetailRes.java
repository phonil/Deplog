package mvp.deplog.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import mvp.deplog.domain.member.WriterInfo;
import mvp.deplog.domain.member.domain.Member;
import mvp.deplog.domain.member.dto.Avatar;
import mvp.deplog.domain.post.domain.Post;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class AnonymousPostDetailRes {

    @Schema(type = "Long", example = "1", description = "게시글 아이디입니다.")
    private Long postId;

    @Schema(type = "String", example = "게시글 제목", description = "게시글 제목입니다.")
    private String title;

    @Schema(type = "LocalDate", example = "2024-08-11", description = "게시글 작성 날짜입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private LocalDate createdDate;

    @Schema(type = "Sting", example = "**게시글 상세 내용**", description = "게시글 내용입니다. 마크다운 형식으로 반환됩니다.")
    private String content;

    @Schema(type = "List<String>", example = "[Spring, Java]", description = "태그 이름 리스트입니다.")
    private List<String> tagNameList;

    @Schema(type = "Integer", example = "0", description = "게시글 조회수입니다.")
    private Integer viewCount;

    @Schema(type = "Integer", example = "0", description = "게시글 좋아요 수입니다.")
    private Integer likeCount;

    @Schema(type = "Integer", example = "0", description = "게시글 스크랩 수입니다.")
    private Integer scrapCount;

    @Schema(type = "WriterInfo", description = "게시글 작성자 처리에 필요한 정보")
    private WriterInfo writerInfo;

    public static AnonymousPostDetailRes of(Post post, Member writer, List<String> tagNameList) {
        return AnonymousPostDetailRes.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .createdDate(post.getCreatedDate().toLocalDate())
                .content(post.getContent())
                .tagNameList(tagNameList)
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .scrapCount(post.getScrapCount())
                .writerInfo(WriterInfo.of(writer))
                .build();
    }
}
