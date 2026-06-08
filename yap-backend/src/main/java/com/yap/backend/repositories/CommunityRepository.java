package com.yap.backend.repositories;

import com.yap.backend.entities.Community;
import com.yap.backend.enums.CommunityCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Integer> {
    boolean existsByName(String name);
    boolean existsByNameIgnoreCase(String name);
    Optional<Community> findByName(String name);
    List<Community> findByCategory(CommunityCategory category);
    List<Community> findByOwner_UserId(Integer userId);
    List<Community> findAllByOrderByCreatedAtDesc();
    List<Community> findByCategoryOrderByCreatedAtDesc(CommunityCategory category);
}