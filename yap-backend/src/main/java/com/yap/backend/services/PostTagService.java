package com.yap.backend.services;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.yap.backend.entities.*;
import com.yap.backend.repositories.PostTagRepository;
import jakarta.transaction.Transactional;

@Service
public class PostTagService {
    private final PostTagRepository postTagRepository;
    private final TagService tagService;

    public PostTagService(PostTagRepository postTagRepository, TagService tagService) {
        this.postTagRepository = postTagRepository;
        this.tagService = tagService;
    }

    @Transactional
    public List<String> saveTags(Post post, List<String> rawTags) {
        if (rawTags == null || rawTags.isEmpty()) return List.of();
        List<String> limited = rawTags.stream().limit(5).toList();
        List<String> savedNames = new ArrayList<>();
        for (String raw : limited) {
            Tag tag = tagService.findOrCreate(raw);
            postTagRepository.save(new PostTag(post, tag));
            savedNames.add(tag.getName());
        }
        return savedNames;
    }

}