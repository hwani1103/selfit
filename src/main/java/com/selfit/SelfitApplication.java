package com.selfit;

import com.selfit.domain.Member;
import com.selfit.member.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class SelfitApplication {

    public static void main(String[] args) {
        SpringApplication.run(SelfitApplication.class, args);
    }



}
