package com.yap.backend.repositories;

import com.yap.backend.entities.Community;
import com.yap.backend.enums.CommunityCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Integer> {

	boolean existsByCommunityName(String communityName);
    boolean existsByCommunityNameIgnoreCase(String communityName);
    Optional<Community> findByCommunityName(String communityName);
 
    // Explore: all communities filtered by category
    List<Community> findByCategory(CommunityCategory category);
 
    // My communities: communities created by this user
    List<Community> findByOwner_UserId(Integer userId);
    
    List<Community> findAllByOrderByCreatedAtDesc();
    
    List<Community> findByCategoryOrderByCreatedAtDesc(CommunityCategory category);
    
    List<Community> findByOwner_UserIdOrderByCreatedAtDesc(Integer userId);
    
}
