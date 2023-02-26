package com.selfit.domain;


import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private String bio;

    private String url;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    private String occupation;

    private String goal;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    private LocalDateTime joinedTime;

    private String validationToken;

    private LocalDateTime tokenCheckedTime;

    private boolean resendTokenFlag;

    private boolean validated;

    public void createToken() {
        this.validationToken = UUID.randomUUID().toString().substring(0, 6);
        this.tokenCheckedTime = LocalDateTime.now();
    }

    public boolean canResendToken() { // 토큰이 만들어진 시간과 현재시간이 1시간 이상 차이가 나야 함.
        if (!resendTokenFlag) {
            resendTokenFlag = true;
            return true;
        } // 플래그가 false면 일단 true로 설정하고 이메일을 다시한번 보낸다.
        // 그 이후에는 무조건 return 1시간.


        return this.tokenCheckedTime.isBefore(LocalDateTime.now().minusHours(1));

    }


}
