package com.selfit.member;

import com.selfit.domain.Member;
import com.selfit.logging.ClassLevelLogging;
import com.selfit.member.form.JoinForm;
import com.selfit.member.form.TokenForm;
import com.selfit.member.validator.JoinFormValidator;
import com.selfit.member.validator.TokenFormValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@Slf4j
@RequiredArgsConstructor
@ClassLevelLogging
public class MemberController {

    private final JoinFormValidator joinFormValidator;
    private final TokenFormValidator tokenFormValidator;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @InitBinder("joinForm")
    public void init(WebDataBinder webDataBinder){
        webDataBinder.addValidators(joinFormValidator);
    }

    @InitBinder("tokenForm")
    public void tokenValidation(WebDataBinder webDataBinder){
        webDataBinder.addValidators(tokenFormValidator);
    }

    @GetMapping("/join")
    public String joinForm(Model model){
        JoinForm joinForm = new JoinForm();
        model.addAttribute("joinForm", joinForm);

        return "member/join";
    }

    @PostMapping("/join")
    public String joinSubmit(@Valid @ModelAttribute JoinForm joinForm, Errors errors){
        if (errors.hasErrors()) {
            return "member/join";
        }

        memberService.newMember(joinForm);
        return "redirect:/check-token";
    }

    @GetMapping("/check-token")
    public String checkToken(Model model){
        TokenForm tokenForm = new TokenForm();
        model.addAttribute(tokenForm);

        return "member/check_token";
    }

    @PostMapping("/check-token")           //redirectAttributes 하면 리다이렉트 받는 메서드에서 띄워주는 뷰까지 살아있음.
    public String checkTokenWithJoinedMember(@ModelAttribute @Valid TokenForm tokenForm, Errors errors,
                                             RedirectAttributes redirectAttributes){
        if(errors.hasErrors()){ //토큰 검증.
            return "member/check_token";
        }

        Member member = memberRepository.findByValidationToken(tokenForm.getValidationToken());
        memberService.defaultSet(member);


        redirectAttributes.addFlashAttribute("message", "회원 가입이 완료되었습니다.");
        return "redirect:/";
    }

    @GetMapping("/resend-token")
    public String resendTokenForm(Model model){ // 인증 메일 재전송하기. 이때는 이메일을 입력받아야 함.
        TokenForm tokenForm = new TokenForm();
        model.addAttribute(tokenForm);
        return "member/resend_token";
    }

    @PostMapping("/resend-token")
    public String resendToken(@ModelAttribute @Valid TokenForm tokenForm, Errors errors){
        if(errors.hasErrors()){
            return "member/resend_token"; // 이메일 검증
        }
        //이메일을 다시 보내준다. 다시 보낼수 있는지 검증하는 로직 나중에 추가필요
        memberService.sendEmail(memberRepository.findByEmail(tokenForm.getEmail()));
        return "redirect:/check-token";
    }

    @GetMapping("/")
    public String home(@CurrentMember Member member, Model model) {
        if (member != null) {
            model.addAttribute(member);
        }

        return "index";
    }

    @GetMapping("/login")
    public String login(){
        return "member/login";
    }

    @GetMapping("/profile/{nickname}")
    public String profilePage(@PathVariable String nickname, Model model,
                              @CurrentMember Member member){
        Member byNickname = memberRepository.findByNickname(nickname);
        if(byNickname == null){
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }

        model.addAttribute(byNickname);
        model.addAttribute("isOwner", byNickname.equals(member));

        return "profile/profile";
    }

}
