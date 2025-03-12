package mvp.deplog.domain.member.application;

import lombok.RequiredArgsConstructor;
import mvp.deplog.domain.member.domain.Member;
import mvp.deplog.domain.member.domain.repository.MemberRepository;
import mvp.deplog.domain.member.dto.mapper.MemberMapper;
import mvp.deplog.domain.member.dto.request.ModifyAvatarReq;
import mvp.deplog.domain.member.dto.response.MyInfoRes;
import mvp.deplog.global.common.Message;
import mvp.deplog.global.common.SuccessResponse;
import mvp.deplog.global.security.UserDetailsImpl;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    private static final String DIRNAME = "avatar";

    @Override
    public SuccessResponse<MyInfoRes> getMyInfo(UserDetailsImpl userDetails) {
        Member member = userDetails.getMember();
        MyInfoRes myInfoRes = memberMapper.memberToMyInfo(member);

        return SuccessResponse.of(myInfoRes);
    }

    @Override
    @Transactional
    public SuccessResponse<Message> modifyAvatar(UserDetailsImpl userDetails, ModifyAvatarReq modifyAvatarReq) {
        Member member = memberRepository.findById(userDetails.getMember().getId())
                .orElseThrow(() -> new BadCredentialsException("잘못된 토큰 입력입니다."));

        member.updateAvatar(modifyAvatarReq);

        Message message = Message.builder()
                .message("아바타 이미지 설정이 완료되었습니다.")
                .build();

        return SuccessResponse.of(message);
    }
}
