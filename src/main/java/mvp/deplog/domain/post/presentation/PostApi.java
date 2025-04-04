package mvp.deplog.domain.post.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import mvp.deplog.domain.member.domain.Part;
import mvp.deplog.domain.post.dto.request.CreatePostReq;
import mvp.deplog.domain.post.dto.response.*;
import mvp.deplog.global.common.Message;
import mvp.deplog.global.common.PageResponse;
import mvp.deplog.global.common.SuccessResponse;
import mvp.deplog.global.exception.ErrorResponse;
import mvp.deplog.global.security.UserDetailsImpl;
import mvp.deplog.infrastructure.s3.dto.response.FileUrlRes;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Post API", description = "게시글 관련 API입니다.")
public interface PostApi {

    @Operation(summary = "게시글 작성 API", description = "게시글 작성을 진행합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", description = "게시글 작성 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Message.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "게시글 작성 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @PostMapping
    ResponseEntity<SuccessResponse<CreatePostRes>> createPost(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Schemas의 CreatePostReq를 참고해주세요.", required = true) @Valid @RequestBody CreatePostReq createPostReq
    );

    @Operation(summary = "게시글 전체 목록 조회 API", description = "게시글 전체 목록을 출력합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "0", description = "조회 성공 - 페이징 dataList 구성",
                    content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PostListRes.class)))}
            ),
            @ApiResponse(
                    responseCode = "200", description = "게시글 목록 조회 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "게시글 목록 조회 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @GetMapping
    ResponseEntity<SuccessResponse<PageResponse>> getAllPost(
            @Parameter(description = "조회할 페이지의 번호를 입력해주세요. **page는 1부터 시작합니다**", required = true) @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "한 페이지 당 최대 항목 개수를 입력해주세요. 기본값은 10입니다.", required = true) @RequestParam(value = "size", defaultValue = "10") Integer size
    );

    @Operation(summary = "게시글 목록 조회 API", description = "게시글 목록을 파트에 맞춰 출력합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "0", description = "조회 성공 - 페이징 dataList 구성",
                    content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PostListRes.class)))}
            ),
            @ApiResponse(
                    responseCode = "200", description = "게시글 목록 조회 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "게시글 목록 조회 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @GetMapping("/{part}")
    ResponseEntity<SuccessResponse<PageResponse>> getPartPost(
            @Parameter(description = "보고싶은 게시글 목록의 파트를 입력해주세요.", required = true) @PathVariable(value = "part") Part part,
            @Parameter(description = "조회할 페이지의 번호를 입력해주세요. **page는 1부터 시작합니다**", required = true) @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "한 페이지 당 최대 항목 개수를 입력해주세요. 기본값은 10입니다.", required = true) @RequestParam(value = "size", defaultValue = "10") Integer size
    );

    @Operation(summary = "게시글 이미지 업로드 후 url 반환 API", description = "게시글 이미지를 업로드 후 url을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "게시글 이미지 업로드 후 url 반환 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = FileUrlRes.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "게시글 이미지 업로드 후 url 반환 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @PostMapping("/uploadImages")
    ResponseEntity<SuccessResponse<FileUrlRes>> uploadImage(
            @Parameter(description = "업로드할 이미지 파일 (Multipart form-data 형식)") @RequestPart(value = "postImage") MultipartFile multipartFile
    ) throws IOException;

    @Operation(summary = "게시글 상세 조회 API", description = "해당 아이디의 게시글을 상세 조회합니다. 회원/비회원에 따라 응답 값이 달라집니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200 ", description = "비회원 - 게시글 상세 조회 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AnonymousPostDetailRes.class))}
            ),
            @ApiResponse(
                    responseCode = "200  ", description = "회원 - 게시글 상세 조회 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = MemberPostDetailRes.class))}
            ),
            @ApiResponse(
                    responseCode = "400   ", description = "게시글 상세 조회 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @GetMapping("/details/{postId}")
    ResponseEntity<SuccessResponse<?>> getPostDetail(
            HttpServletRequest request,
            HttpServletResponse response,
            @Parameter(description = "Access Token을 입력하세요.", required = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "게시글의 번호(아이디)를 입력해주세요.", required = true) @PathVariable(value = "postId") Long postId
    );

    @Operation(summary = "게시글 검색 API", description = "해당 검색어가 포함된 게시글을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "0", description = "조회 성공 - 페이징 dataList 구성",
                    content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PostListRes.class)))}
            ),
            @ApiResponse(
                    responseCode = "200", description = "게시글 검색 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "게시글 검색 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @GetMapping("/searches")
    ResponseEntity<SuccessResponse<PageResponse>> getSearchPosts(
            @Parameter(description = "검색어를 입력해주세요.", required = true) @RequestParam(value = "searchWord") String searchWord,
            @Parameter(description = "조회할 페이지의 번호를 입력해주세요. **page는 1부터 시작합니다**", required = true) @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "한 페이지 당 최대 항목 개수를 입력해주세요. 기본값은 10입니다.", required = true) @RequestParam(value = "size", defaultValue = "10") Integer size
    );

    @Operation(summary = "태그로 게시글 검색 API", description = "해당 태그가 포함된 게시글을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "0", description = "조회 성공 - 페이징 dataList 구성",
                    content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PostListRes.class)))}
            ),
            @ApiResponse(
                    responseCode = "200", description = "태그로 게시글 검색 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "태그로 게시글 검색 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @GetMapping("/searches/tags")
    ResponseEntity<SuccessResponse<PageResponse>> getSearchPostsByTagName(
            @Parameter(description = "태그명을 입력해주세요.", required = true) @RequestParam(value = "tagName") String tagName,
            @Parameter(description = "조회할 페이지의 번호를 입력해주세요. **page는 1부터 시작합니다**", required = true) @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "한 페이지 당 최대 항목 개수를 입력해주세요. 기본값은 10입니다.", required = true) @RequestParam(value = "size", defaultValue = "10") Integer size
    );

    @Operation(summary = "게시글 삭제 API", description = "해당 아이디의 게시글을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "게시글 삭제 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Message.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "게시글 삭제 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @DeleteMapping("/{postId}")
    ResponseEntity<SuccessResponse<Message>> deletePost(
            @Parameter(description = "Access Token을 입력하세요.", required = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "삭제할 게시글의 아이디를 입력하세요.", required = true) @PathVariable(value = "postId") Long postId
    );

    @Operation(summary = "게시글 임시 저장 API", description = "해당 아이디의 게시글을 임시 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", description = "게시글 임시 저장 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CreatePostRes.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "게시글 임시 저장 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @PostMapping("/temps")
    ResponseEntity<SuccessResponse<CreatePostRes>> createTempPost(
            @Parameter(description = "Access Token을 입력하세요.", required = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Schemas의 CreatePostReq를 참고해주세요.", required = true) @Valid @RequestBody CreatePostReq createPostReq
    );

    @Operation(summary = "임시 저장 게시글 발행 API", description = "해당 아이디의 임시 저장 게시글을 벌행합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "임시 저장 게시글 발행 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CreatePostRes.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "임시 저장 게시글 발행 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @PutMapping("/publishing/{postId}")
    ResponseEntity<SuccessResponse<CreatePostRes>> publishTempPost(
            @Parameter(description = "Access Token을 입력하세요.", required = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "발행할 임시 저장 게시글의 아이디를 입력하세요.", required = true) @PathVariable(value = "postId") Long postId,
            @Parameter(description = "Schemas의 CreatePostReq를 참고해주세요.", required = true) @Valid @RequestBody CreatePostReq createPostReq
    );

    @Operation(summary = "임시 저장 게시글 목록 조회 API", description = "해당 아이디의 임시 저장 게시글 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "임시 저장 게시글 목록 조회 성공",
                    content = {@Content(mediaType = "application/json",array = @ArraySchema(schema = @Schema(implementation = TempListRes.class)))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "임시 저장 게시글 목록 조회 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @GetMapping("/temps")
    ResponseEntity<SuccessResponse<List<TempListRes>>> getAllTempPosts(
            @Parameter(description = "Access Token을 입력하세요.", required = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    );

    @Operation(summary = "임시 저장 게시글 상세 조회 API", description = "해당 아이디의 임시 저장 게시글을 상세 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "임시 저장 게시글 상세 조회 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = TempPostDetailRes.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "임시 저장 게시글 상세 조회 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @GetMapping("/temps/details/{postId}")
    ResponseEntity<SuccessResponse<TempPostDetailRes>> getTempPostDetails(
            @Parameter(description = "Access Token을 입력하세요.", required = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "임시 저장 게시글의 아이디를 입력하세요.", required = true) @PathVariable Long postId
    );

    @Operation(summary = "게시글 수정 API", description = "해당 아이디의 게시글을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "게시글 수정 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CreatePostRes.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "게시글 수정 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @PutMapping("/edits/{postId}")
    ResponseEntity<SuccessResponse<CreatePostRes>> modifyPosts(
            @Parameter(description = "Access Token을 입력하세요.", required = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "수정할 게시글의 아이디를 입력하세요.", required = true) @PathVariable(value = "postId") Long postId,
            @Parameter(description = "Schemas의 CreatePostReq를 참고해주세요.", required = true) @Valid @RequestBody CreatePostReq createPostReq
    );
}