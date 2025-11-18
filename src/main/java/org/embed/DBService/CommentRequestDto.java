package org.embed.DBService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequestDto {

    @NotBlank(message = "댓글 내용은 필수 입력입니다.")
    @Size(max = 1000, message = "댓글 내용은 1000자를 초과할 수 없습니다.")
    private String content;

    @NotBlank(message = "닉네임은 필수 입력입니다.")
    @Size(max = 100, message = "닉네임은 100자를 초과할 수 없습니다.")
    private String authorNickname;

    // 💡 비밀번호 유효성 검사 (4자리 숫자만 허용)
    @NotBlank(message = "비밀번호는 필수 입력입니다.")
    @Size(min = 4, max = 4, message = "비밀번호는 4자리여야 합니다.")
    @Pattern(regexp = "^[0-9]*$", message = "비밀번호는 숫자만 입력해야 합니다.")
    private String password; // 평문 비밀번호 (Service에서 해시 처리 예정)
}