package com.yap.backend.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yap.backend.entities.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
	Optional<Tag> findByName(String name);
}
