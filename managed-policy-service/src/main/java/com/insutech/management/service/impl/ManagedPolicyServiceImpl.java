package com.insutech.management.service.impl;


import com.insutech.management.dto.ManagedPolicyRequest;
import com.insutech.management.dto.ManagedPolicyResponse;
import com.insutech.management.entity.ManagedPolicy;
import com.insutech.management.exception.ManagedPolicyNotFoundException;
import com.insutech.management.mapper.ManagedPolicyMapper;
import com.insutech.management.repository.ManagedPolicyRepository;
import com.insutech.management.service.ManagedPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagedPolicyServiceImpl implements ManagedPolicyService {

    private final ManagedPolicyRepository repository;
    private final ManagedPolicyMapper mapper;

    @Override
    public ManagedPolicyResponse createPolicy(ManagedPolicyRequest request) {
        ManagedPolicy policy = mapper.toEntity(request);
        return mapper.toResponse(repository.save(policy));
    }

    @Override
    public ManagedPolicyResponse getPolicyById(Long id) {
        ManagedPolicy policy = repository.findById(id)
                .orElseThrow(() -> new ManagedPolicyNotFoundException(id));
        return mapper.toResponse(policy);
    }

    @Override
    public List<ManagedPolicyResponse> getPolicies(String role, String username) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return repository.findAll().stream().map(mapper::toResponse).toList();
        } else {
            return repository.findByAssignedTo(username).stream().map(mapper::toResponse).toList();
        }
    }

    @Override
    public void deletePolicy(Long id) {
        if (!repository.existsById(id)) {
            throw new ManagedPolicyNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
