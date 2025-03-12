package mvp.deplog.domain.member.presentation;

import lombok.RequiredArgsConstructor;
import mvp.deplog.domain.member.application.MemberService;
import mvp.deplog.domain.member.dto.request.ModifyAvatarReq;
import mvp.deplog.domain.member.dto.response.MyInfoRes;
import mvp.deplog.global.common.Message;
import mvp.deplog.global.common.SuccessResponse;
import mvp.deplog.global.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/members")
public class MemberController implements MemberApi {

    private final MemberService memberService;

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse<MyInfoRes>> getMyInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(memberService.getMyInfo(userDetails));
    }

    @Override
    public ResponseEntity<SuccessResponse<Message>> modifyAvatar(UserDetailsImpl userDetails, ModifyAvatarReq modifyAvatarReq) {
        return ResponseEntity.ok(memberService.modifyAvatar(userDetails, modifyAvatarReq));
    }
}
