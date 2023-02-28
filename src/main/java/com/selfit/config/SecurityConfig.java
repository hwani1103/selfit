package com.selfit.config;


import com.selfit.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@SuppressWarnings("deprecation")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final MemberService memberService;
    private final DataSource dataSource;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //허용할 요청들을 정리한다. HttpMethod.GET도 마찬가지
        http.authorizeRequests()
                .mvcMatchers("/", "/login", "/join", "/check-token", "/resend-token", "/email-check"
                ,"/nickname-check", "/forgot-password").permitAll()
                .mvcMatchers(HttpMethod.GET, "/profile/*").permitAll()
                .anyRequest().authenticated();

        //로그인 옵션을 줄 수 있다.
        http.formLogin()
                .loginPage("/login").permitAll();

        //로그아웃 옵션. 현재 로그아웃 성공하면 "/"로 보내진다.
        http.logout()
                .logoutSuccessUrl("/");

        //리멤버미 토큰 관련. 리멤버미토큰은 지금도 잘 모름
        http.rememberMe()
                .userDetailsService(memberService)
                .tokenRepository(tokenRepository());

    }

    //이거는 정적 파일들 경로 허용하는것.
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .mvcMatchers("/node_modules/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
        web.ignoring().antMatchers("/favicon.ico", "/resources/**", "/error");
    }


    @Bean //리멤버미 토큰.관련. 리포지토리.
    public PersistentTokenRepository tokenRepository(){
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }



}



