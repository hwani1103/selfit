package com.selfit.profile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selfit.domain.Member;
import com.selfit.domain.Tag;
import com.selfit.logging.ClassLevelLogging;
import com.selfit.member.CurrentMember;
import com.selfit.member.MemberRepository;
import com.selfit.member.MemberService;
import com.selfit.member.form.JoinForm;
import com.selfit.profile.form.NicknameForm;
import com.selfit.profile.form.PasswordForm;
import com.selfit.profile.form.ProfileForm;
import com.selfit.profile.form.TagForm;
import com.selfit.profile.validator.NicknameFormValidator;
import com.selfit.profile.validator.PasswordFormValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@ClassLevelLogging
@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final NicknameFormValidator nicknameFormValidator;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;

    @InitBinder("passwordForm")
    public void passwordValidation(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")
    public void nicknameValidation(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameFormValidator);
    }


    @GetMapping("/change/profile")
    public String profileModifyForm(@CurrentMember Member member, Model model) {
        model.addAttribute(member);
        model.addAttribute(new ProfileForm(member));
        return "profile/change-profile";
    }

    @PostMapping("/change/profile")
    public String updateProfile(@CurrentMember Member member,
                                @Valid @ModelAttribute ProfileForm profileForm,
                                Errors errors, Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(member);
            return "profile/change-profile";
        }
        //리다이렉트시키고 한번 쓰고 사라질 데이타. 모델에 자동으로 들어가고 한번 쓰고 없어지는기능. FLash
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        memberService.updateProfile(member, profileForm);
        return "redirect:/change/profile";
    }

    @GetMapping("/change/password")
    public String changePwForm(@CurrentMember Member member, Model model) {
        model.addAttribute(member);
        model.addAttribute(new PasswordForm());

        return "profile/change-password";
    }

    @PostMapping("/change/password")
    public String changePassword(@CurrentMember Member member, @Valid @ModelAttribute PasswordForm passwordForm,
                                 Errors errors, Model model, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute(member);
            return "profile/change-password";
        }

        memberService.updatePassword(member, passwordForm.getNewPassword());
        redirectAttributes.addFlashAttribute("message", "패스워드 변경이 완료되었습니다.");

        return "redirect:/change/password";
    }

    @PostMapping("/passwordConfirm")
    @ResponseBody
    public boolean passwordConfirm(@CurrentMember Member member, @RequestBody PasswordForm passwordForm) {
        return passwordEncoder.matches(passwordForm.getNewPassword(), member.getPassword());

    }

    @GetMapping("/change/nickname")
    public String changeNicknameForm(@CurrentMember Member member, Model model) {
        model.addAttribute(new NicknameForm());
        model.addAttribute(member);

        return "profile/change-nickname";
    }

    @PostMapping("/change/nickname")
    public String changeNickname(@CurrentMember Member member, @Valid @ModelAttribute NicknameForm nicknameForm,
                                 Errors errors, RedirectAttributes redirectAttributes, Model model) {
        if (errors.hasErrors()) {
            model.addAttribute(member);
            return "profile/change-nickname";
        }
        memberService.updateNickname(member, nicknameForm);

        redirectAttributes.addFlashAttribute("message", "닉네임이 성공적으로 변경되었습니다.");

        return "redirect:/change/nickname";

    }

    @GetMapping("/change/tags")
    public String changeTags(@CurrentMember Member member, Model model) throws JsonProcessingException {
        model.addAttribute(member);

        Set<Tag> tags = memberService.getTags(member);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());

        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));
        model.addAttribute("allTags", allTags);
        return "profile/change-tags";
    }

    @PostMapping("/change/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentMember Member member, @RequestBody TagForm tagForm) {

        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if (tag == null) {
           return ResponseEntity.badRequest().build();
        }

        memberService.addTag(member, tag);
        return ResponseEntity.ok().build(); // 200 OK 응답을 보냄. 내용은 없음.

    }

    @PostMapping("/change/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentMember Member member, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if(tag==null){
            return ResponseEntity.badRequest().build();
        }
        memberService.removeTag(member, tag);
        return ResponseEntity.ok().build();

    }


}











