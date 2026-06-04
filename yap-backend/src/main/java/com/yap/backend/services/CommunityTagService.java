package com.yap.backend.services;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.yap.backend.entities.*;
import com.yap.backend.repositories.CommunityTagRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunityTagService {
	private final CommunityTagRepository communityTagRepository;
	private final TagService tagService;
	
	public CommunityTagService(CommunityTagRepository communityTagRepository, TagService tagService) {
		this.communityTagRepository = communityTagRepository;
		this.tagService = tagService;
	}
	
	@Transactional
	public List<String> saveTags(Community community, List<String> rawTags) {
	    if (rawTags == null || rawTags.isEmpty()) return List.of();

	    List<String> limited = rawTags.stream().limit(5).toList();
	    List<String> savedNames = new ArrayList<>();

	    for (String raw : limited) {
	        Tag tag = tagService.findOrCreate(raw);
	        communityTagRepository.save(new CommunityTag(community, tag));
	        savedNames.add(tag.getName());
	    }

	    return savedNames;
	}
	
	@Transactional
	public List<String> updateTags(Community community, List<String> newRawTags) {
		communityTagRepository.deleteByCommunity_CommunityId(community.getCommunityId());
		return saveTags(community, newRawTags);
	}
	
	@Transactional(readOnly = true)
	public List<String> getTrendingTags() {
		return communityTagRepository.findTopTagNames(PageRequest.of(0, 5));
	}

}
