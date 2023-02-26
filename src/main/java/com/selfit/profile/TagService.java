package com.selfit.profile;


import com.selfit.domain.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    @PostConstruct // interest_topics.csv
    public void initTags() throws IOException {
        if (tagRepository.count() == 0) {
            Resource resource = new ClassPathResource("interest_topics.csv");
            List<Tag> tagList = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8).stream()
                    .map(line -> {
                        return Tag.builder().title(line).build();
                                })
                    .collect(Collectors.toList());
            tagRepository.saveAll(tagList);

        }
    }


}
