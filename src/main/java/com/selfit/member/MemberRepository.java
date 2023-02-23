package com.selfit.member;

import com.selfit.domain.Member;
import com.selfit.logging.ClassLevelLogging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@ClassLevelLogging
public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);


    Member findByEmail(String email);

    Member findByValidationToken(String validationToken);


    Member findByNickname(String nickname);
}
