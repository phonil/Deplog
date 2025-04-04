package mvp.deplog.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import mvp.deplog.domain.member.domain.Member;

@Data
@Builder
public class Avatar {

    @Schema(type = "String", example = "face1", description = "아바타 얼굴 파일명입니다.")
    private String avatarFace;

    @Schema(type = "String", example = "body1", description = "아바타 몸통 파일명입니다.")
    private String avatarBody;

    @Schema(type = "String", example = "eyes1", description = "아바타 눈 파일명입니다.")
    private String avatarEyes;

    @Schema(type = "String", example = "nose1", description = "아바타 코 파일명입니다.")
    private String avatarNose;

    @Schema(type = "String", example = "mouth1", description = "아바타 입 파일명입니다.")
    private String avatarMouth;

    public static Avatar of(Member member) {
        return Avatar.builder()
                .avatarFace(member.getAvatarFace())
                .avatarBody(member.getAvatarBody())
                .avatarEyes(member.getAvatarEyes())
                .avatarNose(member.getAvatarNose())
                .avatarMouth(member.getAvatarMouth())
                .build();
    }
}
