package com.selfit.member.validator;


import com.selfit.member.MemberRepository;
import com.selfit.member.MemberService;
import com.selfit.member.form.TokenForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class TokenFormValidator implements Validator {
//밸리데이터 검증 로직은 얼마든지 다양하고 복잡해질 수 있음.
    private final MemberRepository memberRepository;
    private final MemberService memberService;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(TokenForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        TokenForm tokenForm = (TokenForm) target;

        if (tokenForm.getEmail() != null) {
            if (memberRepository.findByEmail(tokenForm.getEmail()) == null) {
                errors.rejectValue("email", "invalid.email", new Object[]{tokenForm.getEmail()}, "가입된 정보가 없거나 잘못된 입력입니다. 다시 확인해주세요.");
                return;
            }

            if (!memberService.canSendToken(tokenForm.getEmail())) {
                errors.rejectValue("email", "invalid.email", new Object[]{tokenForm.getEmail()}, "인증 메일은 첫 재전송 이후 한시간 간격으로 전송 가능합니다.");
            }


        }

        if (tokenForm.getValidationToken() != null) {
            if (memberRepository.findByValidationToken(tokenForm.getValidationToken()) == null) {
                errors.rejectValue("validationToken", "invalid.validationToken", new Object[]{tokenForm.getValidationToken()}, "인증번호를 정확히 입력해 주세요.");
            }
        }

    }
}









