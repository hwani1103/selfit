package com.selfit.profile;

import com.selfit.domain.Member;
import com.selfit.logging.ClassLevelLogging;
import com.selfit.member.CurrentMember;
import com.selfit.member.MemberService;
import com.selfit.profile.form.ProfileForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Slf4j
@ClassLevelLogging
@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final MemberService memberService;


    @GetMapping("/profile/modify")
    public String profileModifyForm(@CurrentMember Member member, Model model) {
        model.addAttribute(member);
        model.addAttribute(new ProfileForm(member));
        return "profile/modify";
    }

    @PostMapping("/profile/modify")
    public String updateProfile(@CurrentMember Member member,
                                @Valid @ModelAttribute ProfileForm profileForm,
                                Errors errors, Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(member);
            return "profile/modify";
        }
        //리다이렉트시키고 한번 쓰고 사라질 데이타. 모델에 자동으로 들어가고 한번 쓰고 없어지는기능. FLash
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        memberService.updateProfile(member, profileForm);
        return "redirect:/profile/modify";
    }



}
