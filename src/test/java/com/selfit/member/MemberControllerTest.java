package com.selfit.member;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;



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

    @DisplayName("회원가입 실패")
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


    @DisplayName("회원가입 성공 - 로그인 처리 X")
    @Test
    void joinSuccess() throws Exception {
        mockMvc.perform(post("/join")
                        .param("nickname", "gogos")
                        .param("email", "email@gmail.com")
                        .param("password", "12345678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(unauthenticated());
                //아직 로그인처리 안했으니깐. unauthenticated. 시큐리티 관점에서..

        //이메일은아직안했음.

    }
}











