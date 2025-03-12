package mvp.deplog.domain.member.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import mvp.deplog.domain.member.dto.request.ModifyAvatarReq;
import mvp.deplog.domain.member.dto.response.MyInfoRes;
import mvp.deplog.global.common.Message;
import mvp.deplog.global.common.SuccessResponse;
import mvp.deplog.global.exception.ErrorResponse;
import mvp.deplog.global.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Member API", description = "회원 관련 API입니다.")
public interface MemberApi {

    @Operation(summary = "내 정보 조회 API", description = "마이페이지에 사용될 내 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "내 정보 조회 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = MyInfoRes.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "내 정보 조회 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @GetMapping
    ResponseEntity<SuccessResponse<MyInfoRes>> getMyInfo(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    );

    @Operation(summary = "아바타 이미지 설정 API", description = "아바타 이미지를 설정합니다. 기존에 아바타가 있는 경우에는 파일명을 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "아바타 이미지 설정 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = MyInfoRes.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "아바타 이미지 설정 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @PutMapping
    ResponseEntity<SuccessResponse<Message>> modifyAvatar(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Schemas의 ModifyAvatarReq를 참고해주세요.", required = true) @RequestBody ModifyAvatarReq modifyAvatarReq
    );
}
