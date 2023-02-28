package com.selfit.member;

import com.selfit.domain.Member;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

public class Principal extends User {
//스프링 시큐리티에서 로그인/로그아웃을 관통하는 principal 객체.
    //실제 회원 엔티티객체를 들고있고 생성자에서 닉네임, 패스워드, 권한을 설정한다.
    //스프링 시큐리티 로그인 로그아웃의 핵심 클래스
    private Member member;

    public Principal(Member member){
        super(member.getNickname(), member.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.member = member;
    }

    public Member getMember() {
        return member;
    }
}
