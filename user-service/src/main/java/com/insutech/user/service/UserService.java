package com.insutech.user.service;

import com.insutech.user.dto.UserRequest;
import com.insutech.user.dto.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserRequest userRequest);
    UserResponse getUserById(Long id);
    List<UserResponse> getUsersByRole(String role);
    List<UserResponse> getAllUsers();
    void deleteUser(Long id);
    UserResponse updateUser(Long id, UserRequest userRequest);
}