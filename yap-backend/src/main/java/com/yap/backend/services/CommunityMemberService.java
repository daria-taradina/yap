package com.yap.backend.services;

import com.yap.backend.entities.*;
import com.yap.backend.enums.CommunityMemberRole;
import com.yap.backend.exceptions.*;
import com.yap.backend.keys.CommunityMemberId;
import com.yap.backend.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunityMemberService extends BaseService {

    private final CommunityMemberRepository communityMemberRepository;
    private final CommunityRepository communityRepository;

    public CommunityMemberService(CommunityMemberRepository communityMemberRepository,
                                   CommunityRepository communityRepository,
                                   UserRepository userRepository) {
        super(userRepository);
        this.communityMemberRepository = communityMemberRepository;
        this.communityRepository = communityRepository;
    }

    @Transactional
    public String joinCommunity(Integer communityId) {
        User currentUser = getAuthenticatedUser();
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Space not found: " + communityId));

        CommunityMemberId memberId = new CommunityMemberId(communityId, currentUser.getUserId());
        if (communityMemberRepository.existsById(memberId)) {
            throw new ResourceAlreadyExistsException("You are already a member of this space");
        }

        communityMemberRepository.save(new CommunityMember(community, currentUser, CommunityMemberRole.MEMBER));
        adjustCounts(community, currentUser, +1);
        return "Joined successfully";
    }

    @Transactional
    public String leaveCommunity(Integer communityId) {
        User currentUser = getAuthenticatedUser();
        CommunityMemberId memberId = new CommunityMemberId(communityId, currentUser.getUserId());
        CommunityMember member = communityMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("You are not a member of this space"));

        if (member.getRole() == CommunityMemberRole.MOD &&
            member.getCommunity().getOwner().getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedException("Space owners cannot leave their own space");
        }

        Community community = member.getCommunity();
        communityMemberRepository.delete(member);
        adjustCounts(community, currentUser, -1);
        return "Left successfully";
    }

    private void adjustCounts(Community community, User user, int delta) {
        community.setMemberCount(Math.max(0, community.getMemberCount() + delta));
        communityRepository.save(community);
        user.setCommunityCount(Math.max(0, user.getCommunityCount() + delta));
        userRepository.save(user);
    }
}