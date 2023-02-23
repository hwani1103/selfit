package com.selfit;


import com.selfit.domain.Member;
import com.selfit.member.MemberRepository;
import com.selfit.member.MemberService;
import com.selfit.member.form.JoinForm;
import com.selfit.member.form.TokenForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

@RequiredArgsConstructor // 이부분 잘 모르겠으면 나중에 프로필 수정 테스트 강좌 다시 보자.
//일단 기능은 WithAccount 커스텀 애너테이션이랑 같이, 단위 테스트 시작하기 전에 얘가 먼저 시작돼서
//인증된 Authentication principal객체를 만들어주는 기능임.
public class WithAccountSecurityContextFactory implements WithSecurityContextFactory<WithAccount> {
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    @Override
    public SecurityContext createSecurityContext(WithAccount withAccount) {
        String nickname = withAccount.value();
        System.out.println("WithAccountSecurityContextFactory 동작");
        JoinForm joinForm = new JoinForm();
        joinForm.setPassword("12345678");
        joinForm.setEmail(nickname + "@aaa.com");
        joinForm.setNickname(nickname);
        Member member = modelMapper.map(joinForm, Member.class);
        member.setValidationToken("aaabbb");
        memberRepository.save(member);

        UserDetails principal = memberService.loadUserByUsername(nickname+"@aaa.com");
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
