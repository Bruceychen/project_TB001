package com.bruceychen.tb001.service;

import com.bruceychen.tb001.dto.UserCreateRequest;
import com.bruceychen.tb001.dto.UserResponse;
import com.bruceychen.tb001.entity.User;
import com.bruceychen.tb001.exception.ResourceNotFoundException;
import com.bruceychen.tb001.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return toResponse(user);
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        User user = new User(request.username());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        userRepository.deleteById(userId);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getUserId(), user.getUsername());
    }
}
