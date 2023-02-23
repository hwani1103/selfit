package com.selfit.member;

import com.selfit.WithAccount;
import com.selfit.domain.Member;
import com.selfit.logging.ClassLevelLogging;
import com.selfit.member.form.TokenForm;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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

    @BeforeEach // 인증 메일 재전송, 가입된 Email과 일치한지 확인하기 위해 미리 Data 저장
    void beforeEach(){
        TokenForm tokenForm = new TokenForm();
        tokenForm.setEmail("bbb@aaa.com");
        Member member = modelMapper.map(tokenForm, Member.class);
        memberRepository.save(member);
    }

    @DisplayName("회원가입 view로 정상 이동")
    @Test
    void joinForm() throws Exception {
        mockMvc.perform(get("/join"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("member/join"))
                .andExpect(model().attributeExists("joinForm"))
                .andExpect(unauthenticated());
    }

    @DisplayName("회원가입 Field 입력 에러")
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

    @DisplayName("회원가입 Field 입력 정상, 인증메일 발송")
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

    @DisplayName("토큰 정상, 인증 완료, 로그인")
    @Test @WithAccount("aaa")
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

    @DisplayName("토큰 비정상, 인증 실패")
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

    @DisplayName("이메일 토큰 재전송 - 이메일 입력 정상")
    @Test
    void validEmail() throws Exception {
        mockMvc.perform(post("/resend-token")
                        .param("email", "bbb@aaa.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/check-token"))
                .andExpect(unauthenticated());

        Member member = memberRepository.findByEmail("bbb@aaa.com");
        assertNotEquals("aaabbb", member.getValidationToken());
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }


    @DisplayName("이메일 토큰 재전송 - 이메일 입력 오류")
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

}











