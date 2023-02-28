package com.selfit.member;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : member")
public @interface CurrentMember {
    //스프링 시큐리티에서 가장 유용하게 사용했던 기능. @CurrentMember Member member 라고 메서드 배개변수에 명시하면
    //현재 로그인되어있는 멤버 객체가 항상 매개변수로 들어온다.
}


