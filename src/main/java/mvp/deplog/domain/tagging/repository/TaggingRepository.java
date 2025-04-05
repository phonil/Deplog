package mvp.deplog.domain.tagging.repository;

import mvp.deplog.domain.post.domain.Post;
import mvp.deplog.domain.tag.domain.Tag;
import mvp.deplog.domain.tagging.Tagging;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaggingRepository extends JpaRepository<Tagging, Long> {

    @Query("SELECT t FROM Tagging t JOIN FETCH t.tag WHERE t.post = :post")
    List<Tagging> findByPost(Post post);

    @Query("SELECT t FROM Tagging t WHERE t.tag = :tag AND t.post.stage = 'PUBLISHED'")
    Page<Tagging> findByTag(Tag tag, Pageable pageable);

    void deleteByPost(Post post);

    List<Tagging> findAllByTag(Tag tag);

    Long countByTag(Tag tag);
}
