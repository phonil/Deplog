package mvp.deplog.domain.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import mvp.deplog.domain.member.domain.Member;
import mvp.deplog.domain.member.domain.Part;
import mvp.deplog.domain.member.dto.Avatar;

@Data
@Builder
public class WriterInfo {

    @Schema(type = "Avatar", description = "작성자의 아바타 이미지 객체입니다.")
    private Avatar avatar;

    @Schema(type = "String", example = "홍길동", description = "작성자의 이름입니다.")
    private String name;

    @Schema(type = "Integer", example = "3", description = "작성자의 기수입니다.")
    private Integer generation;

    @Schema(type = "Enum(Part)", example = "SERVER", description = "작성자의 파트입니다.", allowableValues = {"PLAN", "DESIGN", "ANDROID", "WEB", "SERVER"})
    private Part part;

    public static WriterInfo of(Member writer) {
        return WriterInfo.builder()
                .avatar(Avatar.of(writer))
                .name(writer.getName())
                .generation(writer.getGeneration())
                .part(writer.getPart())
                .build();
    }
}
