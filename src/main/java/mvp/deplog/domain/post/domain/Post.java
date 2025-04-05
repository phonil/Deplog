package mvp.deplog.domain.post.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mvp.deplog.domain.common.BaseEntity;
import mvp.deplog.domain.member.domain.Member;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Getter
@Table(name = "post")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "title") // null 가능 - 임시 저장
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "preview_content")
    private String previewContent;

    @Column(name = "search_content", columnDefinition = "TEXT")
    private String searchContent;

    @Column(name = "preview_image", columnDefinition = "TEXT")
    private String previewImage;

    @Column(name = "like_count")
    @Min(value = 0)
    private Integer likeCount;

    @Column(name = "scrap_count")
    @Min(value = 0)
    private Integer scrapCount;

    @Column(name = "view_count")
    @Min(value = 0)
    private Integer viewCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false)
    private Stage stage;

    @Builder
    public Post(Member member, String content, String previewContent, String searchContent, String previewImage, String title, Stage stage) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.previewContent = previewContent;
        this.searchContent = searchContent;
        this.previewImage = previewImage;
        this.likeCount = 0;
        this.scrapCount = 0;
        this.viewCount = 0;
        this.stage = stage;
    }

    // 게시글 수정
    public void updatePost(String title, String content, String previewContent, String searchContent, String previewImage, Stage stage) {
        this.title = title;
        this.content = content;
        this.previewContent = previewContent;
        this.searchContent = searchContent;
        this.previewImage = previewImage;
        this.stage = stage;
    }

    // 스크랩 수 증가
    public void incrementScrapCount() {
        this.scrapCount++;
    }
    // 스크랩 수 감소
    public void decrementScrapCount() {
        if(this.scrapCount > 0)
            this.scrapCount--;
    }
    // 좋아요 수 증가
    public void incrementLikesCount() {
        this.likeCount++;
    }
    // 좋아요 수 감소
    public void decrementLikesCount() {
        if(this.likeCount > 0)
            this.likeCount--;
    }
    // 조회수 증가
    public void incrementViewCount() {
        this.viewCount++;
    }
}
