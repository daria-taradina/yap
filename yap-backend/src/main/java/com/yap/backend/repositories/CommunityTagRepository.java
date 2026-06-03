package com.yap.backend.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.yap.backend.keys.CommunityTagId;
import com.yap.backend.entities.CommunityTag;

@Repository
public interface CommunityTagRepository extends JpaRepository<CommunityTag, CommunityTagId> {
	void deleteByCommunity_CommunityId(Integer communityId);

	@Query("SELECT ct.tag.name FROM CommunityTag ct GROUP BY ct.tag.name ORDER BY COUNT(ct) DESC")
	java.util.List<String> findTopTagNames(Pageable pageable);
}
