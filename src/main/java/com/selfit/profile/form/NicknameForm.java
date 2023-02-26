package com.selfit.profile.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class NicknameForm {

    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Length(message = " 길이는 최소 2글자에서 최대 10글자 사이입니다.", min = 2, max = 10)
    @Pattern(message = " 특수문자는 _, - 만 허용됩니다.", regexp = "^[ㄱ-ㅎ가-힣a-z0-9_-]{2,10}$")
    private String nickname;

}
