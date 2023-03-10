package com.selfit.member;

import com.selfit.WithAccount;
import com.selfit.domain.Member;
import com.selfit.logging.ClassLevelLogging;
import com.selfit.member.form.TokenForm;
import com.selfit.profile.form.PasswordForm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ClassLevelLogging
class MemberControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ModelMapper modelMapper;

    @MockBean
    JavaMailSender javaMailSender;

    @BeforeEach
        // ?????? ?????? ?????????, ????????? Email??? ???????????? ???????????? ?????? ?????? Data ??????
    void beforeEach() {
        TokenForm tokenForm = new TokenForm();
        tokenForm.setEmail("bbb@aaa.com");
        Member member = modelMapper.map(tokenForm, Member.class);
        memberRepository.save(member);
    }

    @DisplayName("???????????? view??? ?????? ??????")
    @Test
    void joinForm() throws Exception {
        mockMvc.perform(get("/join"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("member/join"))
                .andExpect(model().attributeExists("joinForm"))
                .andExpect(unauthenticated());
    }

    @DisplayName("???????????? Field ?????? ??????")
    @Test
    void joinFail() throws Exception {
        mockMvc.perform(post("/join")
                        .param("nickname", "gogos***")
                        .param("email", "email..")
                        .param("password", "11")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("member/join"))
                .andExpect(unauthenticated());
    }

    @DisplayName("???????????? Field ?????? ??????, ???????????? ??????")
    @Test
    void joinSuccess() throws Exception {
        mockMvc.perform(post("/join")
                        .param("nickname", "gogos")
                        .param("email", "email@gmail.com")
                        .param("password", "12345678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/check-token"))
                .andExpect(unauthenticated());

        Member member = memberRepository.findByEmail("email@gmail.com");
        Assertions.assertNotNull(member);
        assertNotEquals(member.getPassword(), "12345678");

        assertNotNull(member.getValidationToken());
        then(javaMailSender).should().send(any(SimpleMailMessage.class));

    }

    @DisplayName("?????? ??????, ?????? ??????, ?????????")
    @Test
    @WithAccount("gogos")
    void validToken() throws Exception {
        TokenForm tokenForm = new TokenForm();
        tokenForm.setValidationToken("aaabbb");
        mockMvc.perform(post("/check-token")
                        .param("validationToken", "aaabbb")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(authenticated());
    }

    @DisplayName("?????? ?????????, ?????? ??????")
    @Test
    void invalidToken() throws Exception {
        mockMvc.perform(post("/check-token")
                        .param("validationToken", "zzzzzz")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("member/check_token"))
                .andExpect(model().hasErrors())
                .andExpect(unauthenticated());
    }

    @DisplayName("????????? ?????? ????????? - ????????? ?????? ??????")
    @Test
    void validEmail() throws Exception {
        mockMvc.perform(post("/resend-token")
                        .param("email", "bbb@aaa.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/check-token"))
                .andExpect(unauthenticated());

        Member member = memberRepository.findByEmail("bbb@aaa.com");
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }


    @DisplayName("????????? ?????? ????????? - ????????? ?????? ??????")
    @Test
    void invalidEmail() throws Exception {

        mockMvc.perform(post("/resend-token")
                        .param("email", "aaa@@!@#$")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("member/resend_token"))
                .andExpect(model().hasErrors())
                .andExpect(unauthenticated());
    }

    @DisplayName("??????????????? ?????? ????????????")
    @Test
    @WithAccount("gogos")
    void logout() throws Exception {
        mockMvc.perform(post("/resignation")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(unauthenticated());
    }

    @DisplayName("???????????? ?????? ??????")
    @Test
    @WithAccount("gogos")
    void logoutButton() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }

    @DisplayName("???????????? ??????, ????????? ?????? ?????????-??????")
    @Test
    @WithAccount("gogos")
    void forgotPassword() throws Exception {
        Member byEmail = memberRepository.findByEmail("gogos@aaa.com");
        byEmail.setValidated(true);

        mockMvc.perform(post("/resend-token")
                        .with(csrf())
                        .param("email", "gogos@aaa.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/forgot-password"));
        // ??? ????????????, ????????? ????????? ????????????, ??? ??????????????? ?????? ????????? ???????????? ???????????? ??????
        // ?????? ???????????? OK????????? ???????????? ????????? ????????????.. ??? ??? ?????? ??? ???????????????
        String token = memberRepository.findByEmail("gogos@aaa.com").getValidationToken();

        System.out.println(token);
        mockMvc.perform(post("/forgot-password")
                        .param("validationToken", token)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated());

    }

    @DisplayName("?????? ????????? ??????")
    @WithAccount("gogos")
    @Test
    void viewProfile() throws Exception{
        mockMvc.perform(get("/profile/gogos"))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("isOwner"))
                .andExpect(model().attributeExists("allTags"))
                .andExpect(status().isOk());
    }
}










