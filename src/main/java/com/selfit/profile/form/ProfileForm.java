package com.selfit.profile.form;

import com.selfit.domain.Member;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;


@Data
@NoArgsConstructor
public class ProfileForm {

    @Length(message = " 길이는 최대 20글자 입니다.", max = 20)
    private String bio;

    @Length(message = " 길이는 최대 40글자 입니다.", max = 40)
    private String url;

    @Length(message = " 길이는 최대 10글자 입니다.", max = 10)
    private String occupation;

    @Length(message = " 길이는 최대 10글자 입니다.", max = 10)
    private String goal;

    private String profileImage;

    public ProfileForm(Member member) {
        this.bio = member.getBio();
        this.url = member.getUrl();
        this.occupation = member.getOccupation();
        this.goal = member.getGoal();
        this.profileImage = member.getProfileImage();
    }
}
