package com.selfit.profile;

import com.selfit.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Tag findByTitle(String title);


}
