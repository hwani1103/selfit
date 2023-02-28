package com.selfit.member;

import com.selfit.domain.Member;
import com.selfit.domain.Tag;
import com.selfit.logging.ClassLevelLogging;
import com.selfit.member.form.JoinForm;
import com.selfit.profile.form.NicknameForm;
import com.selfit.profile.form.ProfileForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@ClassLevelLogging
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;

    public Member newMember(JoinForm joinForm) {
        joinForm.setPassword(passwordEncoder.encode(joinForm.getPassword()));
        Member newMember = modelMapper.map(joinForm, Member.class);
        newMember.setTokenCheckedTime(LocalDateTime.now());
        sendEmail(newMember);
        return memberRepository.save(newMember);
    }

    public void sendEmail(Member newMember) {
        newMember.createToken();
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newMember.getEmail());
        mailMessage.setSubject("Selfit, Join Validation");
        mailMessage.setText("/check-token?token=" + newMember.getValidationToken()
                + "&email=" + newMember.getEmail());
        javaMailSender.send(mailMessage);
    }



    public void defaultSet(Member member) {
        member.setValidated(true);
        member.setJoinedTime(LocalDateTime.now());
        login(member);
    }

    public void login(Member member) {
    //이 login의 경우 /login post요청 외에 특정 비즈니스 로직상 로그인을 시켜야 할 경우에 수행된다.
        //예를들어 토큰 인증이 완료되면 로그인할 경우.
        //이렇게 로그인하나 밑에 loadUserByUsername을 사용하나 동일한 로그인 결과가 나옴.
        //더 디테일하게는 아직 모름
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(
                        new Principal(member),
                        member.getPassword(),
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Override // 스프링 시큐리티의 /login post요청을 받는 메서드.
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email);

        if (member == null) {
            throw new UsernameNotFoundException(email);
        }

        if(!member.isValidated())
            throw new IllegalArgumentException("미인증 회원");

        return new Principal(member);
    }

    public void updateProfile(Member member, ProfileForm profileForm) {
        modelMapper.map(profileForm, member);
        memberRepository.save(member);

    }

    public void updatePassword(Member member, String newPassword) {
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    public Member updateNickname(Member member, NicknameForm nicknameForm) {
        member.setNickname(nicknameForm.getNickname());
        memberRepository.save(member);
        login(member);
        return member;

    }

    public void resignation(Member member) {
        memberRepository.delete(member);
    }

    public boolean canSendToken(String email) {

        Member byEmail = memberRepository.findByEmail(email);

        if(byEmail==null){
            return false;
        }
        return byEmail.canResendToken();
    }

    public Member changePasswordByToken(String validationToken) {
        Member byValidationToken = memberRepository.findByValidationToken(validationToken);
        byValidationToken.setPassword(passwordEncoder.encode(validationToken));
        return byValidationToken;
    }

    public void addTag(Member member, Tag tag){
        Optional<Member> byId = memberRepository.findById(member.getId());
        byId.ifPresent(a->a.getTags().add(tag));
    }

    public Set<Tag> getTags(Member member){
        Optional<Member> byId = memberRepository.findById(member.getId());
        return byId.orElseThrow().getTags();
    }

    public void removeTag(Member member, Tag tag){
        Optional<Member> byId = memberRepository.findById(member.getId());
        byId.ifPresent(a -> a.getTags().remove(tag));
    }



}
