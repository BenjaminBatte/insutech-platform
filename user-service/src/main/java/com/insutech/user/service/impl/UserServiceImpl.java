package com.insutech.user.service.impl;

import com.insutech.user.dto.UserRequest;
import com.insutech.user.dto.UserResponse;
import com.insutech.user.entity.User;
import com.insutech.user.enums.Role;
import com.insutech.user.exception.InvalidRoleException;
import com.insutech.user.exception.UserAlreadyExistsException;
import com.insutech.user.exception.UserNotFoundException;
import com.insutech.user.mapper.UserMapper;
import com.insutech.user.repository.UserRepository;
import com.insutech.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    @CacheEvict(value = {"users", "usersByRole", "allUsers"}, allEntries = true)
    public UserResponse createUser(UserRequest userRequest) {
        validateUserRequest(userRequest);

        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + userRequest.getUsername());
        }

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + userRequest.getEmail());
        }

        User user = userMapper.toEntity(userRequest);
        User savedUser = userRepository.save(user);

        log.info("User created successfully: {}", savedUser.getUsername());
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Cacheable(value = "users", key = "#id")   // ✅ Cache by ID
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toResponse(user);
    }

    @Override
    @Cacheable(value = "usersByRole", key = "#role")  // ✅ Cache by role
    public List<UserResponse> getUsersByRole(String role) {
        try {
            Role userRole = Role.valueOf(role.toUpperCase());
            List<User> users = userRepository.findByRole(userRole);
            return userMapper.toResponseList(users);
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleException("Invalid role: " + role);
        }
    }

    @Override
    @Cacheable(value = "allUsers", key = "'all'")  // ✅ Cache all users
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.toResponseList(users);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "usersByRole", "allUsers"}, allEntries = true) // ✅ Clear caches
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
        log.info("User deleted with id: {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "usersByRole", "allUsers"}, allEntries = true) // ✅ Clear caches
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        validateUserRequest(userRequest);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (userRepository.existsByUsernameAndIdNot(userRequest.getUsername(), id)) {
            throw new UserAlreadyExistsException("Username already exists: " + userRequest.getUsername());
        }

        if (userRepository.existsByEmailAndIdNot(userRequest.getEmail(), id)) {
            throw new UserAlreadyExistsException("Email already exists: " + userRequest.getEmail());
        }

        existingUser.setUsername(userRequest.getUsername());
        existingUser.setEmail(userRequest.getEmail());
        existingUser.setRole(userRequest.getRole());

        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully: {}", updatedUser.getUsername());
        return userMapper.toResponse(updatedUser);
    }

    private void validateUserRequest(UserRequest userRequest) {
        if (userRequest.getRole() == null) {
            throw new InvalidRoleException("Role cannot be null");
        }
    }
}
