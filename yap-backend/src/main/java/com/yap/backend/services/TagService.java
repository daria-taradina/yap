package com.yap.backend.services;

import org.springframework.stereotype.Service;
import com.yap.backend.entities.Tag;
import com.yap.backend.exceptions.*;
import com.yap.backend.repositories.TagRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TagService {
	private final TagRepository tagRepository;
	
	public TagService(TagRepository tagRepository) {
		this.tagRepository = tagRepository;
	}
	
	@Transactional
	public Tag findOrCreate(String rawName) {
		String tagName = rawName.toLowerCase().replaceAll("[^a-z0-9]", "");
		
		if(tagName.isEmpty()) {
		    throw new InvalidInputException("Invalid tag name: " + rawName);
		}
		return tagRepository.findByName(tagName)
				.orElseGet(() -> tagRepository.save(new Tag(tagName)));
	}

}
