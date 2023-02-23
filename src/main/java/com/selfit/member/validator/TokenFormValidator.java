package com.selfit.member.validator;


import com.selfit.member.MemberRepository;
import com.selfit.member.form.TokenForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class TokenFormValidator implements Validator {

    private final MemberRepository memberRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(TokenForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        TokenForm tokenForm = (TokenForm) target;
        if (tokenForm.getEmail() != null) {
            if (!memberRepository.existsByEmail(tokenForm.getEmail())) {
                errors.rejectValue("email", "invalid.email", new Object[]{tokenForm.getEmail()}, "가입 정보가 없는 이메일입니다.");
            }
        }

        if (tokenForm.getValidationToken() != null) {
            System.out.println("=============================");
            if (memberRepository.findByValidationToken(tokenForm.getValidationToken()) == null) {
                errors.rejectValue("validationToken", "invalid.validationToken", new Object[]{tokenForm.getValidationToken()}, "인증번호를 정확히 입력해 주세요.");
            }
        }

    }
}









