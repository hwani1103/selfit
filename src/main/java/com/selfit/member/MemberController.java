package com.selfit.member;

import com.selfit.domain.Member;
import com.selfit.member.form.JoinForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MemberController {

    private final JoinFormValidator joinFormValidator;
    private final MemberService memberService;

    @InitBinder("joinForm")
    public void init(WebDataBinder webDataBinder){
        webDataBinder.addValidators(joinFormValidator);
    }


    @GetMapping("/join")
    public String joinForm(Model model){
        JoinForm joinForm = new JoinForm();
        model.addAttribute("joinForm", joinForm);

        return "member/join";
    }

    @PostMapping("/join")
    public String joinForm(@Valid @ModelAttribute JoinForm joinForm, Errors errors){
        if (errors.hasErrors()) {
            return "member/join";
        }

        Member member = new Member();
        member.setNickname(joinForm.getNickname());
        member.setPassword(joinForm.getPassword());
        member.setEmail(joinForm.getEmail());

        memberService.save(member);
        log.info("닉네임 = {}", joinForm.getNickname());
        log.info("패스워드 = {}", joinForm.getPassword());
        log.info("이메일 = {}", joinForm.getEmail());

        return "redirect:/";
    }


}
