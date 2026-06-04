package com.yap.backend.services;

import com.yap.backend.entities.User;
import com.yap.backend.exceptions.UnauthorizedException;
import com.yap.backend.repositories.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class BaseService {

    protected final UserRepository userRepository;

    public BaseService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
    }
}