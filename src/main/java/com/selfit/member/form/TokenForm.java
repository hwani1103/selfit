package com.selfit.member.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TokenForm {

    //토큰 전송 폼. 이 폼객체는 토큰만 검증할떄도 사용되고, 이메일만 검증할때도 사용된다.

    private String email;

    private String validationToken;

}
