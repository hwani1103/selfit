package com.selfit.member.form;


import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class JoinForm {

    @Email
    @NotBlank(message = "이메일을 입력해 주세요.")
    private String email;

    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Length(message = " 길이는 최소 2글자에서 최대 10글자 사이입니다.", min = 2, max = 10)
    @Pattern(message = " 특수문자는 _, - 만 허용됩니다.", regexp = "^[ㄱ-ㅎ가-힣a-z0-9_-]{2,10}$")
    private String nickname;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Length(message = "비밀번호는 8자리 이상, 20자리 이하로 입력해 주세요.", min = 8, max=20)
    private String password;

    
}


//
//@Data
//public class SignUpForm {
//
//    @NotBlank(message = "공백은 허용되지 않습니다.")
//    @Length(message = " 길이는 최소 3글자에서 최대 20글자 사이입니다.", min = 3, max = 20)
//    @Pattern(message = " 특수문자는 _, - 만 허용됩니다.", regexp = "^[ㄱ-ㅎ가-힣a-z0-9_-]{3,20}$")
//    private String nickname;
//
//    @Email
//    @NotBlank
//    private String email;
//
//    @NotBlank
//    @Length(min = 8, max = 50)
//    private String password;
//
//
//}