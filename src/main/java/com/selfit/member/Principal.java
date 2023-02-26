package com.selfit.member;

import com.selfit.domain.Member;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

public class Principal extends User {

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
