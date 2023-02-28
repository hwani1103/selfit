package com.selfit.member;

import com.selfit.domain.Member;
import com.selfit.logging.ClassLevelLogging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@ClassLevelLogging
public interface MemberRepository extends JpaRepository<Member, Long> {
//Data JPA 기능. 리포지토리. 대부분의 쿼리는 자동으로 제공된다.
//내가 특별히 만든 Field들에 대해서는 관련 문법에 맞게 필드이름만 넣어주면 그대로 쿼리가 나감.
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);


    Member findByEmail(String email);

    Member findByValidationToken(String validationToken);


    Member findByNickname(String nickname);
}
