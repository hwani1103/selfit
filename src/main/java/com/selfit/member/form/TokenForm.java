package com.selfit.member.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TokenForm {

    private String email;

    private String validationToken;

}
