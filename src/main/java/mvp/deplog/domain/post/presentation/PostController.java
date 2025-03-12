package mvp.deplog.domain.post.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mvp.deplog.domain.member.domain.Part;
import mvp.deplog.domain.post.application.PostDetailService;
import mvp.deplog.domain.post.application.PostDetailServiceFactory;
import mvp.deplog.domain.post.application.PostService;
import mvp.deplog.domain.post.dto.request.CreatePostReq;
import mvp.deplog.domain.post.dto.response.CreatePostRes;
import mvp.deplog.domain.post.dto.response.TempListRes;
import mvp.deplog.domain.post.dto.response.TempPostDetailRes;
import mvp.deplog.global.common.Message;
import mvp.deplog.global.common.PageResponse;
import mvp.deplog.global.common.SuccessResponse;
import mvp.deplog.global.security.UserDetailsImpl;
import mvp.deplog.infrastructure.s3.dto.response.FileUrlRes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/posts")
public class PostController implements PostApi {

    private final PostService postService;
    private final PostDetailServiceFactory postDetailServiceFactory;

    @Override
    @PostMapping
    public ResponseEntity<SuccessResponse<CreatePostRes>> createPost(
                    @AuthenticationPrincipal UserDetailsImpl userDetails,
             @Valid @RequestBody CreatePostReq createPostReq
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(postService.createPost(userDetails.getMember(), createPostReq));
    }

    @Override
    @GetMapping("/all")
    public ResponseEntity<SuccessResponse<PageResponse>> getAllPost(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return ResponseEntity.ok(postService.getAllPosts(page-1, size));
    }

    @Override
    @GetMapping("/{part}")
    public ResponseEntity<SuccessResponse<PageResponse>> getPartPost(
            @PathVariable("part") Part part,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return ResponseEntity.ok(postService.getPosts(part, page-1, size));
    }

    @Override
    @PostMapping("/uploadImages")
    public ResponseEntity<SuccessResponse<FileUrlRes>> uploadImage(
            @RequestPart(value = "postImage") MultipartFile multipartFile
    ) throws IOException {
        return ResponseEntity.ok(postService.uploadImages(multipartFile));
    }

    @Override
    @GetMapping("/details/{postId}")
    public ResponseEntity<SuccessResponse<?>> getPostDetail(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("postId") Long postId
    ) {
        PostDetailService<?> postDetailService = postDetailServiceFactory.find(userDetails);
        return ResponseEntity.ok(postDetailService.getPostDetail(userDetails, postId));
    }

    @Override
    @GetMapping("/searches")
    public ResponseEntity<SuccessResponse<PageResponse>> getSearchPosts(
            @RequestParam("searchWord") String searchWord,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return ResponseEntity.ok(postService.getSearchPosts(searchWord, page-1, size));
    }

    @Override
    @GetMapping("/searches/tags")
    public ResponseEntity<SuccessResponse<PageResponse>> getSearchPostsByTagName(
            @RequestParam("tagName") String tagName,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return ResponseEntity.ok(postService.getSearchPostsByTag(tagName, page-1, size));
    }

    @Override
    @DeleteMapping("/{postId}")
    public ResponseEntity<SuccessResponse<Message>> deletePost(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable(value = "postId") Long postId
    ) {
        return ResponseEntity.ok(postService.deletePost(userDetails.getMember().getId(), postId));
    }

    @Override
    @PostMapping("/temps")
    public ResponseEntity<SuccessResponse<CreatePostRes>> createTempPost(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CreatePostReq createPostReq
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(postService.createTempPost(userDetails.getMember(), createPostReq));
    }

    @Override
    @PutMapping("/publishing/{postId}")
    public ResponseEntity<SuccessResponse<CreatePostRes>> publishTempPost(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable(value = "postId") Long postId,
            @Valid @RequestBody CreatePostReq createPostReq
    ) {
        return ResponseEntity.ok(postService.publishTempPost(userDetails.getMember().getId(), postId, createPostReq));
    }

    @Override
    @GetMapping("/temps")
    public ResponseEntity<SuccessResponse<List<TempListRes>>> getAllTempPosts(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(postService.getAllTempPosts(userDetails.getMember().getId()));
    }

    @Override
    @GetMapping("/temps/details/{postId}")
    public ResponseEntity<SuccessResponse<TempPostDetailRes>> getTempPostDetails(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable(value = "postId") Long postId
    ) {
        return ResponseEntity.ok(postService.getTempPostDetails(userDetails, postId));
    }

    @Override
    @PutMapping("/edits/{postId}")
    public ResponseEntity<SuccessResponse<CreatePostRes>> modifyPosts(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable(value = "postId") Long postId,
            @Valid @RequestBody CreatePostReq createPostReq
    ) {
        return ResponseEntity.ok(postService.modifyPost(userDetails.getMember().getId(), postId, createPostReq));
    }
}
