package mvp.deplog.domain.post.application;

import mvp.deplog.domain.post.domain.Post;
import mvp.deplog.domain.post.domain.repository.PostRepository;
import mvp.deplog.domain.post.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AnonymousPostDetailServiceTest {

    @Autowired
    private AnonymousPostDetailServiceImpl anonymousPostDetailService ;
    @Autowired
    private PostRepository postRepository;
    private final int THREAD_COUNT = 100;
    private static Long POST_ID = 1L;

    @Test
    @DisplayName("단순 + 1: 쓰레드 100 테스트")
    void 단순_카운팅_쓰레드_100_테스트() throws InterruptedException {
        Post post = postRepository.findById(POST_ID).orElseThrow();
        System.out.println("시작 조회수 = " + post.getViewCount());
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    anonymousPostDetailService.basic(POST_ID);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();
        Post afterPost = postRepository.findById(POST_ID).orElseThrow();
        System.out.println("최종 조회수 = " + afterPost.getViewCount());
        assertThat(post.getViewCount()).isEqualTo(afterPost.getViewCount() - THREAD_COUNT);
    }

    @Test
    @DisplayName("비관적 락: 쓰레드 100 테스트")
    void 비관적_락_쓰레드_100_테스트() throws InterruptedException {
        Post post = postRepository.findById(POST_ID).orElseThrow();
        System.out.println("시작 조회수 = " + post.getViewCount());
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    anonymousPostDetailService.pLock(POST_ID);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();
        Post afterPost = postRepository.findById(POST_ID).orElseThrow();
        System.out.println("최종 조회수 = " + afterPost.getViewCount());
        assertThat(post.getViewCount()).isEqualTo(afterPost.getViewCount() - THREAD_COUNT);
    }

    @Test
    @DisplayName("낙관적 락: 쓰레드 100 테스트")
    void 낙관적_락_쓰레드_100_테스트() throws InterruptedException {
        Post post = postRepository.findById(POST_ID).orElseThrow();
        System.out.println("시작 조회수 = " + post.getViewCount());
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    try {
                        while (true) {
                            try {
                                anonymousPostDetailService.oLock(POST_ID);
                                break;
                            } catch (ObjectOptimisticLockingFailureException e) {
                                Thread.sleep(1);
                            }
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();
        Post afterPost = postRepository.findById(POST_ID).orElseThrow();
        System.out.println("최종 조회수 = " + afterPost.getViewCount());
        assertThat(post.getViewCount()).isEqualTo(afterPost.getViewCount() - THREAD_COUNT);
    }

    @Test
    @DisplayName("Update 쿼리: 쓰레드 100 테스트")
    void 업데이트_쿼리_쓰레드_100_테스트() throws InterruptedException {
        Post post = postRepository.findById(POST_ID).orElseThrow();
        System.out.println("시작 조회수 = " + post.getViewCount());
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    anonymousPostDetailService.updateQuery(POST_ID);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();
        Post afterPost = postRepository.findById(POST_ID).orElseThrow();
        System.out.println("최종 조회수 = " + afterPost.getViewCount());
        assertThat(post.getViewCount()).isEqualTo(afterPost.getViewCount() - THREAD_COUNT);
    }
}
