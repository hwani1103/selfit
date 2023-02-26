package com.selfit.member.validator;


import com.selfit.member.MemberRepository;
import com.selfit.member.form.JoinForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class JoinFormValidator implements Validator {

    private final MemberRepository memberRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(JoinForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        JoinForm joinForm = (JoinForm) target;
        if(memberRepository.existsByEmail(joinForm.getEmail())){
            errors.rejectValue("email", "invalid.email", new Object[]{joinForm.getEmail()}, "이미 사용중인 이메일입니다.");
        }

        if(memberRepository.existsByNickname(joinForm.getNickname())){
            errors.rejectValue("nickname", "invalid.nickname", new Object[]{joinForm.getNickname()}, "이미 사용중인 닉네임입니다.");
        }

    }
}









