package com.selfit.domain;


import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter @Setter
@EqualsAndHashCode(of="id") @Builder
@AllArgsConstructor @NoArgsConstructor
public class Tag {

    //관심주제 태그. 관심주제 태그는 개별적인 생명주기를 갖기때문에 멤버의 밸류보다는
    //엔티티로 설계

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String title;


}

