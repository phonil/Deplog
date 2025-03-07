package mvp.deplog.domain.auth.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mvp.deplog.domain.auth.dto.request.JoinReq;
import mvp.deplog.domain.auth.dto.request.LoginReq;
import mvp.deplog.domain.auth.dto.request.LogoutReq;
import mvp.deplog.domain.auth.dto.request.ModifyPasswordReq;
import mvp.deplog.domain.auth.dto.response.EmailDuplicateCheckRes;
import mvp.deplog.domain.auth.dto.response.LoginRes;
import mvp.deplog.domain.auth.dto.response.ReissueRes;
import mvp.deplog.global.common.Message;
import mvp.deplog.global.common.SuccessResponse;
import mvp.deplog.global.exception.ErrorResponse;
import mvp.deplog.global.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth API", description = "인증 관련 API입니다.")
public interface AuthApi {

    @Operation(summary = "회원 가입 API", description = "회원 가입을 진행합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", description = "회원가입 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Message.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "회원가입 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @PostMapping(value = "/join")
    ResponseEntity<SuccessResponse<Message>> join(
            @Parameter(description = "Schemas의 JoinReq를 참고해주세요.", required = true) @Valid @RequestBody JoinReq joinReq
    );

    @Operation(summary = "로그인 API", description = "로그인을 진행합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "로그인 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = LoginRes.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "로그인 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @PostMapping(value = "/login")
    ResponseEntity<SuccessResponse<LoginRes>> login(
            @Parameter(description = "Schemas의 LoginReq를 참고해주세요.", required = true) @Valid @RequestBody LoginReq loginReq
    );

    @Operation(summary = "로그아웃 API", description = "로그아웃을 진행합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "로그아웃 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Message.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "로그아웃 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @DeleteMapping(value = "/logout")
    ResponseEntity<SuccessResponse<Message>> logout(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Schemas의 LogoutReq를 참고해주세요.", required = true) @RequestBody LogoutReq logoutReq
    );

    @Operation(summary = "회원 탈퇴 API", description = "회원 탈퇴를 진행합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "회원 탈퇴 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Message.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "회원 탈퇴 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @DeleteMapping(value = "/exit")
    ResponseEntity<SuccessResponse<Message>> exit(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    );

    @Operation(summary = "토큰 재발급 API", description = "리프레시 토큰으로 액세스 토큰 재발급을 진행합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "토큰 재발급 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ReissueRes.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "토큰 재발급 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @GetMapping(value = "/reissue")
    ResponseEntity<SuccessResponse<ReissueRes>> reissue(
            @Parameter(description = "리프레시 토큰을 입력해주세요.", required = true) @RequestParam(value = "refreshToken") String refreshToken
    );

    @Operation(summary = "이메일 중복 체크 API", description = "이메일 중복 여부를 체크합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "이메일 중복 체크 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = EmailDuplicateCheckRes.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "이메일 중복 체크 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @GetMapping(value = "/emails")
    ResponseEntity<SuccessResponse<EmailDuplicateCheckRes>> checkEmailDuplicate (
            @Parameter(description = "검사할 이메일을 입력해주세요.", required = true) @RequestParam(value = "email") String email
    );

    @Operation(summary = "비밀번호 변경 API", description = "비밀번호를 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "비밀번호 변경 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Message.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "비밀번호 변경 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })

    @PutMapping(value = "/password")
    ResponseEntity<SuccessResponse<Message>> modifyPassword (
            @Parameter(description = "비밀번호를 변경할 계정의 이메일을 입력해주세요.", required = true) @Valid @RequestBody ModifyPasswordReq modifyPasswordReq
    );
}
