package mvp.deplog.domain.auth.application;

import mvp.deplog.domain.auth.dto.request.JoinReq;
import mvp.deplog.domain.auth.dto.request.LoginReq;
import mvp.deplog.domain.auth.dto.request.LogoutReq;
import mvp.deplog.domain.auth.dto.request.ModifyPasswordReq;
import mvp.deplog.domain.auth.dto.response.EmailDuplicateCheckRes;
import mvp.deplog.domain.auth.dto.response.LoginRes;
import mvp.deplog.domain.auth.dto.response.ReissueRes;
import mvp.deplog.global.common.Message;
import mvp.deplog.global.common.SuccessResponse;
import mvp.deplog.global.security.UserDetailsImpl;

public interface AuthService {

    SuccessResponse<Message> join(JoinReq joinReq);

    SuccessResponse<LoginRes> login(LoginReq loginReq);

    SuccessResponse<Message> logout(UserDetailsImpl userDetails, LogoutReq logoutReq);

    SuccessResponse<Message> exit(UserDetailsImpl userDetails);

    SuccessResponse<ReissueRes> reissue(String refreshToken);

    SuccessResponse<EmailDuplicateCheckRes> checkEmailDuplicate(String email);

    SuccessResponse<Message> modifyPassword(ModifyPasswordReq modifyPasswordReq);
}
