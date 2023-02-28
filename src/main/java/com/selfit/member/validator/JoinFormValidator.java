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
//회원가입 밸리데이터. Validator인터페이스를 구현한다.
    private final MemberRepository memberRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(JoinForm.class);
    } // 서포트 메서드는 간단하다. 현재 들어온 클래스타입이 내가 검증할 타입인지를 검사함.

    @Override
    public void validate(Object target, Errors errors) {
        //밸리데이트 메서드는 내 마음대로 검증로직을 구현하면 됨. 주로 단순 필드값 검증보다는 중복확인 등의 추가적인 로직이 필요한 경우에 설정
        JoinForm joinForm = (JoinForm) target;
        if(memberRepository.existsByEmail(joinForm.getEmail())){
            errors.rejectValue("email", "invalid.email", new Object[]{joinForm.getEmail()}, "이미 사용중인 이메일입니다.");
        }

        if(memberRepository.existsByNickname(joinForm.getNickname())){
            errors.rejectValue("nickname", "invalid.nickname", new Object[]{joinForm.getNickname()}, "이미 사용중인 닉네임입니다.");
        }

    }
}









