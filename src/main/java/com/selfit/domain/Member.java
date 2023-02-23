package com.selfit.domain;


import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Getter @Setter
@EqualsAndHashCode(of="id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Member {

    @Id @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private String bio;

    private String url;

    private String occupation;

    private String goal;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    private LocalDateTime joinedTime;

    private String validationToken;

    private LocalDateTime tokenCheckedTime;

    private boolean validated;

    public void createToken(){
        this.validationToken = UUID.randomUUID().toString().substring(0, 6);
        this.tokenCheckedTime = LocalDateTime.now();
    }






}
