package com.selfit.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    //Bean으로 수동 등록하는것들은 대부분 내가 직접 구현하지 않는 애들이다.
    //MemberController, MemberService 이런애들은 그냥 애너테이션으로 자동 등록함.
    //구체 클래스가 내가 사용할 빈이기 때문
    //이경우 수동등록이지만 스프링이 알아서 PasswordEncoder 빈을 등록해준다.
    //모델맵퍼도 마찬가지

    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }
    //모델맵퍼는 폼객체에서 엔티티 객체로 값을 옮길 때 유용하게 사용할 수 있다.
    //주의해야 할 점은 원본객체가 가지고 있는 필드들에 대해서 폼객체는 항상 값을 가지고 있어야 함.
    //예를들어 nickname이라는 필드를 폼객체와 엔티티객체가 모두 가지고 있는데, 폼객체에는 nickname필드가 할당이 안되어서 null이라면
    //나도모르게 엔티티의 nickname이 null이 된다. 그리고 엔티티에 nickname이 없는데 폼에는 nickname이 있으면 아마 에러날듯
}
