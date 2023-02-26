package com.selfit.profile.validator;

import com.selfit.member.MemberRepository;
import com.selfit.profile.form.NicknameForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class NicknameFormValidator implements Validator {

    private final MemberRepository memberRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(NicknameForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        NicknameForm nicknameForm = (NicknameForm) target;

        if(memberRepository.findByNickname(nicknameForm.getNickname())!=null){
            errors.rejectValue("nickname", "wrong.value", "이미 사용중인 닉네임입니다.");
        }
    }
}
