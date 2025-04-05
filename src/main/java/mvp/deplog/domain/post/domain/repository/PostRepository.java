package mvp.deplog.domain.post.domain.repository;

import jakarta.persistence.LockModeType;
import mvp.deplog.domain.member.domain.Member;
import mvp.deplog.domain.member.domain.Part;
import mvp.deplog.domain.post.domain.Post;
import mvp.deplog.domain.post.domain.Stage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.id = :postId")
    Optional<Post> findByIdForUpdate(@Param("postId") Long postId);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT p FROM Post p WHERE p.id = :postId")
    Optional<Post> findByIdOLock(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    @Query("SELECT p FROM Post p JOIN FETCH p.member WHERE p.id = :id AND p.stage = :stage")
    Optional<Post> findByIdAndStage(Long id, Stage stage);

    Page<Post> findAllByStage(Stage stage, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.member.part IN :parts AND p.stage = 'PUBLISHED'")
    Page<Post> findByMemberPart(@Param("parts") List<Part> partGroup, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE (p.title LIKE %:titleSearchWord% OR p.searchContent LIKE %:contentSearchWord%) " +
            "AND p.stage = 'PUBLISHED'")
    Page<Post> findByTitleContainingOrSearchContentContaining(@Param("titleSearchWord") String titleSearchWord,
                                                              @Param("contentSearchWord") String contentSearchWord,
                                                              Pageable pageable);

    List<Post> findByMemberAndStageOrderByCreatedDateDesc(Member member, Stage stage);

    List<Post> findByMember(Member member);
}