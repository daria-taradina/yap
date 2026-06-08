package com.yap.backend.services;

import com.yap.backend.dtos.*;
import com.yap.backend.entities.*;
import com.yap.backend.enums.CommunityCategory;
import com.yap.backend.enums.CommunityMemberRole;
import com.yap.backend.exceptions.*;
import com.yap.backend.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommunityService extends BaseService {

    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final CommunityTagService communityTagService;

    public CommunityService(CommunityRepository communityRepository,
                            CommunityMemberRepository communityMemberRepository,
                            CommunityTagService communityTagService,
                            UserRepository userRepository) {
        super(userRepository);
        this.communityRepository = communityRepository;
        this.communityMemberRepository = communityMemberRepository;
        this.communityTagService = communityTagService;
    }

    @Transactional
    public CommunityResponse createCommunity(CreateCommunity dto) {
        User currentUser = getAuthenticatedUser();
        String name = dto.getName().toLowerCase();

        if (communityRepository.existsByNameIgnoreCase(name)) {
            throw new ResourceAlreadyExistsException("Space name already taken: " + name);
        }

        Community community = new Community();
        community.setName(name);
        community.setDescription(dto.getDescription());
        community.setOwner(currentUser);
        community.setCategory(dto.getCategory());
        community.setMemberCount(1);

        if (dto.getGuidelines() != null && !dto.getGuidelines().isEmpty()) {
            String joined = dto.getGuidelines().stream()
                .filter(g -> g != null && !g.isBlank())
                .collect(Collectors.joining(","));
            community.setGuidelines(joined);
        }

        Community saved = communityRepository.save(community);
        List<String> tagNames = communityTagService.saveTags(saved, dto.getTags());

        communityMemberRepository.save(
            new CommunityMember(saved, currentUser, CommunityMemberRole.MOD)
        );

        currentUser.setCommunityCount(currentUser.getCommunityCount() + 1);
        userRepository.save(currentUser);

        return mapToResponseDTO(saved, tagNames, currentUser.getUserId());
    }

    @Transactional(readOnly = true)
    public List<CommunityResponse> getAllCommunities() {
        User currentUser = getAuthenticatedUser();
        return communityRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(c -> mapToResponseDTO(c, currentUser.getUserId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommunityResponse> getCommunitiesByCategory(String categoryParam) {
        User currentUser = getAuthenticatedUser();
        CommunityCategory category = parseCategoryOrThrow(categoryParam);
        return communityRepository.findByCategoryOrderByCreatedAtDesc(category)
                .stream()
                .map(c -> mapToResponseDTO(c, currentUser.getUserId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommunityResponse> getMyCommunities() {
        User currentUser = getAuthenticatedUser();
        Integer userId = currentUser.getUserId();
        return communityMemberRepository.findByUser_UserId(userId)
                .stream()
                .sorted(Comparator
                    .comparing((CommunityMember m) ->
                        m.getCommunity().getOwner().getUserId().equals(userId) ? 0 : 1)
                    .thenComparing(CommunityMember::getJoinedAt, Comparator.reverseOrder()))
                .map(m -> mapToResponseDTO(m.getCommunity(), userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CommunityResponse getCommunityByName(String name) {
        User currentUser = getAuthenticatedUser();
        Community community = communityRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Space not found: " + name));
        return mapToResponseDTO(community, currentUser.getUserId());
    }

    @Transactional(readOnly = true)
    public List<String> getTrendingTags() {
        return communityTagService.getTrendingTags();
    }

    // helpers
    private CommunityCategory parseCategoryOrThrow(String param) {
        try {
            return CommunityCategory.valueOf(param.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Unknown category: " + param);
        }
    }

    private CommunityResponse mapToResponseDTO(Community c, List<String> tagNames, Integer currentUserId) {
        boolean isMember = communityMemberRepository.existsById(
            new com.yap.backend.keys.CommunityMemberId(c.getCommunityId(), currentUserId));
        return new CommunityResponse(
            c.getCommunityId(), c.getName(), c.getDescription(),
            c.getMemberCount(), c.getCreatedAt(),
            c.getOwner().getUserId(), c.getOwner().getUsername(),
            c.getCategory(), c.getIconUrl(), c.getBannerUrl(),
            c.getGuidelines(), tagNames, isMember
        );
    }

    private CommunityResponse mapToResponseDTO(Community c, Integer currentUserId) {
        List<String> tagNames = c.getTags().stream()
            .map(ct -> ct.getTag().getName())
            .collect(Collectors.toList());
        return mapToResponseDTO(c, tagNames, currentUserId);
    }
}