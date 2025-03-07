package mvp.deplog.domain.post.application;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mvp.deplog.domain.comment.domain.repository.CommentRepository;
import mvp.deplog.domain.likes.domain.repository.LikesRepository;
import mvp.deplog.domain.member.domain.Member;
import mvp.deplog.domain.member.domain.Part;
import mvp.deplog.domain.member.domain.repository.MemberRepository;
import mvp.deplog.domain.member.dto.Avatar;
import mvp.deplog.domain.post.domain.Post;
import mvp.deplog.domain.post.domain.Stage;
import mvp.deplog.domain.post.domain.repository.PostRepository;
import mvp.deplog.domain.post.dto.request.CreatePostReq;
import mvp.deplog.domain.post.dto.response.CreatePostRes;
import mvp.deplog.domain.post.dto.response.PostListRes;
import mvp.deplog.domain.post.dto.response.TempListRes;
import mvp.deplog.domain.post.dto.response.TempPostDetailRes;
import mvp.deplog.domain.post.exception.ResourceNotFoundException;
import mvp.deplog.domain.post.exception.UnauthorizedException;
import mvp.deplog.domain.scrap.domain.repository.ScrapRepository;
import mvp.deplog.domain.tag.domain.Tag;
import mvp.deplog.domain.tag.domain.repository.TagRepository;
import mvp.deplog.domain.tagging.Tagging;
import mvp.deplog.domain.tagging.repository.TaggingRepository;
import mvp.deplog.global.common.Message;
import mvp.deplog.global.common.PageInfo;
import mvp.deplog.global.common.PageResponse;
import mvp.deplog.global.common.SuccessResponse;
import mvp.deplog.global.security.UserDetailsImpl;
import mvp.deplog.infrastructure.markdown.MarkdownUtil;
import mvp.deplog.infrastructure.s3.application.FileUploader;
import mvp.deplog.infrastructure.s3.dto.response.FileUrlRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static mvp.deplog.domain.post.constant.PostConstant.DIRNAME;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PostService {

    private final FileUploader fileUploader;

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final TaggingRepository taggingRepository;
    private final LikesRepository likesRepository;
    private final ScrapRepository scrapRepository;

    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public SuccessResponse<CreatePostRes> createPost(Member member, CreatePostReq createPostReq) {
        validateTagName(createPostReq.getTagNameList());

        String content = createPostReq.getContent();
        String previewContent = MarkdownUtil.extractPreviewContent(content);
        String previewImage = MarkdownUtil.extractPreviewImage(content);
        String searchContent = MarkdownUtil.extractPlainText(content);

        Post post = Post.builder()
                .member(member)
                .title(createPostReq.getTitle())
                .content(content)
                .previewContent(previewContent)
                .searchContent(searchContent)
                .previewImage(previewImage)
                .stage(Stage.PUBLISHED)
                .build();
        // 게시글 저장
        Post savePost = postRepository.save(post);

        // 태그 저장 & Tagging 엔티티 연결
        for(String tagName : createPostReq.getTagNameList()) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() ->
                            tagRepository.save(Tag.builder().name(tagName).build()));

            Tagging tagging = Tagging.builder()
                    .post(post)
                    .tag(tag)
                    .build();

            taggingRepository.save(tagging);
        }

        CreatePostRes createPostRes = CreatePostRes.builder()
                .postId(savePost.getId())
                .build();

        return SuccessResponse.of(createPostRes);
    }

    // 태그명 중복 검사 메서드
    private void validateTagName(List<String> tagNameList) {
        Set<String> tagNameSet = new HashSet<>();
        for(String tagName : tagNameList) {
            if(!tagNameSet.add(tagName))
                throw new IllegalArgumentException("중복되는 태그명이 있습니다: " + tagName);
        }
    }

    public SuccessResponse<PageResponse> getAllPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Post> posts = postRepository.findAllByStage(Stage.PUBLISHED, pageable);

        Page<PostListRes> postList = postList(posts);

        PageInfo pageInfo = PageInfo.toPageInfo(pageable, posts);
        PageResponse pageResponse = PageResponse.toPageResponse(pageInfo, postList.getContent());

        return SuccessResponse.of(pageResponse);
    }

    private Page<PostListRes> postList(Page<Post> posts) {
        return posts.map(post -> PostListRes.builder()
                .id(post.getId())
                .title(post.getTitle())
                .previewContent(post.getPreviewContent())
                .previewImage(post.getPreviewImage())
                .createdDate(post.getCreatedDate().toLocalDate())
                .avatar(Avatar.builder()
                        .avatarFace(post.getMember().getAvatarFace())
                        .avatarBody(post.getMember().getAvatarBody())
                        .avatarEyes(post.getMember().getAvatarEyes())
                        .avatarNose(post.getMember().getAvatarNose())
                        .avatarMouth(post.getMember().getAvatarMouth())
                        .build())
                .name(post.getMember().getName())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .scrapCount(post.getScrapCount())
                .build());
    }

    public SuccessResponse<PageResponse> getPosts(Part part, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Post> posts;

        posts = postList(part, pageable);

        Page<PostListRes> postList = postList(posts);

        PageInfo pageInfo = PageInfo.toPageInfo(pageable, posts);
        PageResponse pageResponse = PageResponse.toPageResponse(pageInfo, postList.getContent());

        return SuccessResponse.of(pageResponse);
    }

    // 개발 관련 파트(WEB, ANDROID, SERVER) Grouping
    private Page<Post> postList(Part part, Pageable pageable) {
        Page<Post> posts;
        List<Part> partGroup;
        switch (part){
            case WEB, ANDROID, SERVER -> partGroup = List.of(Part.WEB, Part.ANDROID, Part.SERVER);
            default -> partGroup = List.of(part);
        }
        posts = postRepository.findByMemberPart(partGroup, pageable);

        return posts;
    }

    public SuccessResponse<FileUrlRes> uploadImages(MultipartFile multipartFile) throws IOException {
        String filePath = fileUploader.uploadMultipartFile(multipartFile, DIRNAME);
        FileUrlRes fileUrlRes = FileUrlRes.builder()
                .fileUrl(filePath)
                .build();
        return SuccessResponse.of(fileUrlRes);
    }

    public SuccessResponse<FileUrlRes> uploadFile(MultipartFile multipartFile) throws IOException {
        String filePath = fileUploader.uploadMultipartFile(multipartFile, DIRNAME);
        FileUrlRes fileUrlRes = FileUrlRes.builder()
                .fileUrl(filePath)
                .build();
        return SuccessResponse.of(fileUrlRes);
    }

    public SuccessResponse<FileUrlRes> uploadStreamFile(HttpServletRequest request) throws IOException{
        String filePath = fileUploader.uploadStreamFile(request, DIRNAME);
        FileUrlRes fileUrlRes = FileUrlRes.builder()
                .fileUrl(filePath)
                .build();
        return SuccessResponse.of(fileUrlRes);
    }

    public SuccessResponse<PageResponse> getSearchPosts(String searchWord, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Post> posts = postRepository.findByTitleContainingOrSearchContentContaining(searchWord, searchWord, pageable);

        Page<PostListRes> searchPostList = postList(posts);

        PageInfo pageInfo = PageInfo.toPageInfo(pageable, posts);
        PageResponse pageResponse = PageResponse.toPageResponse(pageInfo, searchPostList.getContent());

        return SuccessResponse.of(pageResponse);
    }

    public SuccessResponse<PageResponse> getSearchPostsByTag(String tagName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Tag tag = tagRepository.findByName(tagName)
                .orElseThrow(() -> new IllegalArgumentException("해당 이름의 태그가 없습니다: " + tagName));

        Page<Tagging> taggingPosts = taggingRepository.findByTag(tag, pageable);

        Page<PostListRes> searchPostList = taggingPosts.map(tagging -> {
            Post post = tagging.getPost();
            return PostListRes.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .previewContent(post.getPreviewContent())
                    .previewImage(post.getPreviewImage())
                    .createdDate(post.getCreatedDate().toLocalDate())
                    .avatar(Avatar.builder()
                            .avatarFace(post.getMember().getAvatarFace())
                            .avatarBody(post.getMember().getAvatarBody())
                            .avatarEyes(post.getMember().getAvatarEyes())
                            .avatarNose(post.getMember().getAvatarNose())
                            .avatarMouth(post.getMember().getAvatarMouth())
                            .build())
                    .name(post.getMember().getName())
                    .viewCount(post.getViewCount())
                    .likeCount(post.getLikeCount())
                    .scrapCount(post.getScrapCount())
                    .build();
        });

        PageInfo pageInfo = PageInfo.toPageInfo(pageable, taggingPosts);
        PageResponse pageResponse = PageResponse.toPageResponse(pageInfo, searchPostList.getContent());

        return SuccessResponse.of(pageResponse);
    }

    @Transactional
    public SuccessResponse<Message> deletePost(Long memberId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 id의 게시글을 찾을 수 없습니다: " + postId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 id의 멤버를 찾을 수 없습니다: " + memberId));

        if(!post.getMember().equals(member)) {
            throw new UnauthorizedException("본인이 작성한 게시글이 아니므로 삭제할 수 없습니다.");
        }

        // 게시글 내 대댓글 삭제
        commentRepository.deleteByPostAndParentCommentIsNotNull(post);

        // 게시글 내 댓글 삭제
        commentRepository.deleteByPostAndParentCommentIsNull(post);

        // 게시글 내 태그 삭제
        taggingRepository.deleteByPost(post);

        // 게시글 스크랩 내용 삭제
        scrapRepository.deleteByPost(post);

        // 게시글 좋아요 내용 삭제
        likesRepository.deleteByPost(post);

        // 게시글 삭제
        postRepository.delete(post);

        Message message = Message.builder()
                .message("게시글 삭제가 완료되었습니다.")
                .build();

        return SuccessResponse.of(message);
    }

    @Transactional
    public SuccessResponse<CreatePostRes> createTempPost(Member member, CreatePostReq createPostReq) {
        String content = createPostReq.getContent();
        String previewContent = MarkdownUtil.extractPreviewContent(content);
        String previewImage = MarkdownUtil.extractPreviewImage(content);
        String searchContent = MarkdownUtil.extractPlainText(content);

        Post post = Post.builder()
                .member(member)
                .title(createPostReq.getTitle())
                .content(content)
                .previewContent(previewContent)
                .searchContent(searchContent)
                .previewImage(previewImage)
                .stage(Stage.TEMP)
                .build();

        Post savePost = postRepository.save(post);

        taggingRepository.deleteByPost(post);

        // 태그 저장 & Tagging 엔티티 연결
        for(String tagName : createPostReq.getTagNameList()) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() ->
                            tagRepository.save(Tag.builder().name(tagName).build()));

            Tagging tagging = Tagging.builder()
                    .post(post)
                    .tag(tag)
                    .build();

            taggingRepository.save(tagging);
        }

        CreatePostRes createPostRes = CreatePostRes.builder()
                .postId(savePost.getId())
                .build();

        return SuccessResponse.of(createPostRes);
    }

    @Transactional
    public SuccessResponse<CreatePostRes> publishTempPost(Long memberId, Long postId, CreatePostReq createPostReq) {
        validateTagName(createPostReq.getTagNameList());

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 id의 게시글을 찾을 수 없습니다: " + postId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 id의 멤버를 찾을 수 없습니다: " + memberId));

        if(!post.getMember().equals(member)) {
            throw new UnauthorizedException("본인이 작성한 게시글이 아니므로 발행할 수 없습니다.");
        }

        String content = createPostReq.getContent();
        String previewContent = MarkdownUtil.extractPreviewContent(content);
        String previewImage = MarkdownUtil.extractPreviewImage(content);
        String searchContent = MarkdownUtil.extractPlainText(content);

        post.updatePost(createPostReq.getTitle(), content, previewContent, searchContent, previewImage, Stage.PUBLISHED);

        taggingRepository.deleteByPost(post);

        // 태그 저장 & Tagging 엔티티 연결
        for(String tagName : createPostReq.getTagNameList()) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));

            Tagging tagging = Tagging.builder()
                    .post(post)
                    .tag(tag)
                    .build();

            taggingRepository.save(tagging);
        }

        CreatePostRes createPostRes = CreatePostRes.builder()
                .postId(postId)
                .build();

        return SuccessResponse.of(createPostRes);
    }

    public SuccessResponse<List<TempListRes>> getAllTempPosts(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 id의 멤버를 찾을 수 없습니다: " + memberId));

        List<Post> posts = postRepository.findByMemberAndStageOrderByCreatedDateDesc(member, Stage.TEMP);

        List<TempListRes> tempList = posts.stream()
                .map(post -> TempListRes.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .createdDate(post.getCreatedDate().toLocalDate())
                        .build())
                .collect(Collectors.toList());

        return SuccessResponse.of(tempList);
    }

    @Transactional
    public SuccessResponse<TempPostDetailRes> getTempPostDetails(UserDetailsImpl userDetails, Long postId) {
        Long memberId = userDetails.getMember().getId();
        Post post = postRepository.findByIdAndStage(postId, Stage.TEMP)
                .orElseThrow(() -> new ResourceNotFoundException("해당 id의 게시글을 찾을 수 없습니다: " + postId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 id의 멤버를 찾을 수 없습니다: " + memberId));

        Member writer = post.getMember();
        if(!member.equals(writer)) {
            throw new UnauthorizedException("본인이 작성한 임시저장 게시글만 조회할 수 있습니다.");
        }

        List<String> tagNameList = taggingRepository.findByPost(post).stream()
                .map(tagging -> tagging.getTag().getName())
                .collect(Collectors.toList());

        TempPostDetailRes tempPostDetailRes = TempPostDetailRes.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .tagNameList(tagNameList)
                .build();

        return SuccessResponse.of(tempPostDetailRes);
    }

    @Transactional
    public SuccessResponse<CreatePostRes> modifyPost(Long memberId, Long postId, CreatePostReq createPostReq) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 id의 게시글을 찾을 수 없습니다: " + postId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 id의 멤버를 찾을 수 없습니다: " + memberId));

        if(!post.getMember().equals(member)) {
            throw new UnauthorizedException("본인이 작성한 게시글이 아니므로 수정할 수 없습니다.");
        }
        if(post.getStage().equals(Stage.PUBLISHED)) {
            validateTagName(createPostReq.getTagNameList());
        }

        String content = createPostReq.getContent();
        String previewContent = MarkdownUtil.extractPreviewContent(content);
        String previewImage = MarkdownUtil.extractPreviewImage(content);
        String searchContent = MarkdownUtil.extractPlainText(content);

        // 수정 전 게시글 내 이미지 url 추출
        List<String> oldImageUrls = MarkdownUtil.extractImageLinks(post.getContent());

        post.updatePost(createPostReq.getTitle(), content, previewContent, searchContent, previewImage, post.getStage());

        // 수정 후 게시글 내 이미지 url 추출
        List<String> newImageUrls = MarkdownUtil.extractImageLinks(content);

        // 기존 이미지 url이 수정된 게시글에 있는지 확인
        for(String oldImageUrl : oldImageUrls) {
            if(!newImageUrls.contains(oldImageUrl)) {
                fileUploader.deleteFile(oldImageUrl, DIRNAME);
            }
        }

        taggingRepository.deleteByPost(post);

        for (String tagName : createPostReq.getTagNameList()) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));

            Tagging tagging = Tagging.builder()
                    .post(post)
                    .tag(tag)
                    .build();

            taggingRepository.save(tagging);
        }

        CreatePostRes createPostRes = CreatePostRes.builder()
                .postId(postId)
                .build();

        return SuccessResponse.of(createPostRes);
    }
}