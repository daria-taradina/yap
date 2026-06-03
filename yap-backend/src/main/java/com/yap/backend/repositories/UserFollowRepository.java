package com.yap.backend.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.yap.backend.keys.*;
import com.yap.backend.entities.*;

public interface UserFollowRepository extends JpaRepository<UserFollow, UserFollowId> {
	// List of users that the given user is FOLLOWING (current user -> others)
	List<UserFollow> findByFollower_UserId(Integer followerId);
	// List of users that are FOLLOWING the given user (others -> current user)
	List<UserFollow> findByFollowing_UserId(Integer followingId);
}
