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
        // 인증 메일 재전송, 가입된 Email과 일치한지 확인하기 위해 미리 Data 저장
    void beforeEach() {
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

    @DisplayName("회원탈퇴로 인한 로그아웃")
    @Test
    @WithAccount("gogos")
    void logout() throws Exception {
        mockMvc.perform(post("/resignation")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(unauthenticated());
    }

    @DisplayName("로그아웃 버튼 클릭")
    @Test
    @WithAccount("gogos")
    void logoutButton() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }

    @DisplayName("비밀번호 분실, 이메일 인증 로그인-성공")
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
        // 위 코드에서, 새로운 토큰을 보내니까, 그 토큰값으로 해당 회원의 토큰값이 바뀌어야 되고
        // 그걸 가져와서 OK한다음 로그인이 되어야 하는건데.. 아 잘 된다 뭐 착각했었네
        String token = memberRepository.findByEmail("gogos@aaa.com").getValidationToken();

        System.out.println(token);
        mockMvc.perform(post("/forgot-password")
                        .param("validationToken", token)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated());

    }

    @DisplayName("회원 프로필 조회")
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










