package com.selfit.profile;


import com.selfit.WithAccount;
import com.selfit.domain.Member;
import com.selfit.member.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @AfterEach
    void afterEach() {
        memberRepository.deleteAll();
    }

    @WithAccount("gogos")
    @DisplayName("프로필 수정 폼")
    @Test
    void updateProfileForm() throws Exception {
        mockMvc.perform(get("/change/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("profileForm"));
    }

    @WithAccount("gogos")
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile() throws Exception {
        String bio = "짧은 소개를 수정하는 경우.";
        mockMvc.perform(post("/change/profile")
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/change/profile"))
                .andExpect(flash().attributeExists("message"));

        Member gogos = memberRepository.findByNickname("gogos");
        assertEquals(bio, gogos.getBio());
    }

    @WithAccount("gogos")
    @DisplayName("프로필 수정하기 - 입력값 에러")
    @Test
    void updateProfile_error() throws Exception {
        String bio = "길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 너무나도 길게 소개를 수정하는 경우. ";
        mockMvc.perform(post("/change/profile")
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/change-profile"))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(model().hasErrors());

        Member gogos = memberRepository.findByNickname("gogos");
        assertNull(gogos.getBio());
    }


    @WithAccount("gogos")
    @DisplayName("비밀번호 변경 - 입력값 정상")
    @Test
    void changePwSuccess() throws Exception {
        mockMvc.perform(post("/change/password")
                        .with(csrf())
                        .param("newPassword", "123456789")
                        .param("newPasswordConfirm", "123456789"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/change/password"));

        Member member = memberRepository.findByNickname("gogos");
        assertTrue(passwordEncoder.matches("123456789", member.getPassword()));
        //비밀번호가 제대로 변경 되었는지 확인

    }

    @WithAccount("gogos")
    @DisplayName("비밀번호 변경 - 실패")
    @Test
    void changePwFail1() throws Exception {
        mockMvc.perform(post("/change/password")
                        .with(csrf())
                        .param("newPassword", "99781232")
                        .param("newPasswordConfirm", "1"))
                .andExpect(model().attributeExists("member"))
                .andExpect(status().isOk());

        Member member = memberRepository.findByNickname("gogos");
        assertTrue(passwordEncoder.matches("12345678", member.getPassword()));
        // 원래 WithAccountFactory에서 설정한 비밀번호 "12345678" 즉 변경되지 않았어야 정상
    }

    @WithAccount("gogos")
    @DisplayName("닉네임 변경 - 입력값 정상")
    @Test
    void nicknamePwSuccess() throws Exception {
        Member member1 = new Member();
        member1.setNickname("깡깡스");
        member1.setEmail("bbb@bbb.com");
        memberRepository.save(member1);

        mockMvc.perform(post("/change/nickname")
                        .with(csrf())
                        .param("nickname", "깽깽스"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/change/nickname"));

        Member member = memberRepository.findByEmail("gogos@aaa.com");
        assertTrue(member.getNickname().equals("깽깽스"));
        //"깽깽스" 닉네임을 사용하는 회원이 없으므로 정상 변경 완료

    }

    @WithAccount("gogos")
    @DisplayName("닉네임 변경 - 입력값 중복")
    @Test
    void nicknamePwFail() throws Exception {
        Member member1 = new Member();
        member1.setNickname("깡깡스");
        member1.setEmail("bbb@bbb.com");
        memberRepository.save(member1);

        mockMvc.perform(post("/change/nickname")
                        .with(csrf())
                        .param("nickname", "깡깡스"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("member"));

        Member member = memberRepository.findByEmail("gogos@aaa.com");
        assertTrue(member.getNickname().equals("gogos"));
        //변경하려는 "깡깡스" 라는 닉네임이 이미 있어서, 변경되지 않고 기존의 "gogos"로 있어야 함.

    }

    @WithAccount("gogos")
    @DisplayName("태그 전체 조회")
    @Test
    void findTags() throws Exception{
        mockMvc.perform(get("/change/tags"))
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("allTags"))
                .andExpect(status().isOk());


    }



}