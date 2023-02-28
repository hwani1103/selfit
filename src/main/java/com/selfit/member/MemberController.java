package com.selfit.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.selfit.domain.Member;
import com.selfit.domain.Tag;
import com.selfit.logging.ClassLevelLogging;
import com.selfit.member.form.JoinForm;
import com.selfit.member.form.TokenForm;
import com.selfit.member.validator.JoinFormValidator;
import com.selfit.member.validator.TokenFormValidator;
import com.selfit.profile.TagRepository;
import com.selfit.profile.form.PasswordForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
@ClassLevelLogging
public class MemberController {

    private final JoinFormValidator joinFormValidator;
    private final TokenFormValidator tokenFormValidator;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    @InitBinder("joinForm")
    public void init(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(joinFormValidator);
    }

    @InitBinder("tokenForm")
    public void tokenValidation(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(tokenFormValidator);
    }

    @GetMapping("/join")
    public String joinForm(Model model) {
        JoinForm joinForm = new JoinForm();
        model.addAttribute("joinForm", joinForm);

        return "member/join";
    }

    @PostMapping("/join")
    public String joinSubmit(@Valid @ModelAttribute JoinForm joinForm, Errors errors) {
        if (errors.hasErrors()) {
            return "member/join";
        }

        memberService.newMember(joinForm);
        return "redirect:/check-token";
    }

    @GetMapping("/check-token")
    public String checkToken(Model model) {
        TokenForm tokenForm = new TokenForm();
        model.addAttribute(tokenForm);

        return "member/check_token";
    }

    @PostMapping("/check-token")           //redirectAttributes 하면 리다이렉트 받는 메서드에서 띄워주는 뷰까지 살아있음.
    public String checkTokenWithJoinedMember(@ModelAttribute @Valid TokenForm tokenForm, Errors errors,
                                             RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) { //토큰 검증.
            return "member/check_token";
        }

        Member member = memberRepository.findByValidationToken(tokenForm.getValidationToken());
        memberService.defaultSet(member);


        redirectAttributes.addFlashAttribute("message", "회원 가입이 완료되었습니다.");
        return "redirect:/";
    }

    @GetMapping("/resend-token")
    public String resendTokenForm(Model model) { // 인증 메일 재전송하기. 이때는 이메일을 입력받아야 함.
        TokenForm tokenForm = new TokenForm();
        model.addAttribute(tokenForm);
        return "member/resend_token";
    }

    @PostMapping("/resend-token")
    public String resendToken(@CurrentMember Member member, @ModelAttribute @Valid TokenForm tokenForm, Errors errors) {
        if (errors.hasErrors()) {
            return "member/resend_token"; // 이메일 검증
        }

        Member byEmail = memberRepository.findByEmail(tokenForm.getEmail());
        if (byEmail.isValidated()) {
            memberService.sendEmail(byEmail);
            return "redirect:/forgot-password";
        }
        memberService.sendEmail(byEmail);
        return "redirect:/check-token";
    }

    @GetMapping("/forgot-password")
    public String tokenLogin(Model model) {
        TokenForm tokenForm = new TokenForm();
        model.addAttribute(tokenForm);
        return "member/forgot_password";
    }

    @PostMapping("/forgot-password")
    public String tokenLoginSubmit(@Valid @ModelAttribute TokenForm tokenForm, Errors errors,
                                   RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) { //이메일 검증.
            return "member/forgot_password";
        }

        Member member = memberService.changePasswordByToken(tokenForm.getValidationToken());
        redirectAttributes.addFlashAttribute("message1", "비밀번호를 변경해 주세요. 현재 비밀번호는 인증 토큰값입니다.");

        memberService.login(member);
        return "redirect:/";

    }

    @GetMapping("/")
    public String home(@CurrentMember Member member, Model model) {
        if (member != null) {
            model.addAttribute(member);
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "member/login";
    }


    @GetMapping("/profile/{nickname}")
    public String profilePage(@PathVariable String nickname, Model model,
                              @CurrentMember Member member) {
        Member byNickname = memberRepository.findByNickname(nickname);
        if (byNickname == null) {
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }

        model.addAttribute(byNickname);
        model.addAttribute("isOwner", byNickname.equals(member));
        Set<Tag> tags = memberService.getTags(byNickname);

        model.addAttribute("allTags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        return "profile/profile";
    }

    @PostMapping("/resignationConfirm")
    @ResponseBody
    public boolean resignationConfirm(@CurrentMember Member member, @RequestBody PasswordForm passwordForm) {
        return passwordEncoder.matches(passwordForm.getNewPassword(), member.getPassword());
    }

    @PostMapping("/resignation")
    @ResponseBody
    public boolean resignation(@CurrentMember Member member, HttpServletRequest request, HttpServletResponse response) {

        memberService.resignation(member);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return true;
    }

    @PostMapping("/email-check")
    @ResponseBody
    public boolean emailCheck(@RequestBody JoinForm joinForm) {
        return memberRepository.existsByEmail(joinForm.getEmail());

    }

    @PostMapping("/nickname-check")
    @ResponseBody
    public boolean nicknameCheck(@RequestBody JoinForm joinForm) {
        return memberRepository.existsByNickname(joinForm.getNickname());
    }

}
