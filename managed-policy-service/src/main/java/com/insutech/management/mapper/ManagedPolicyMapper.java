package com.insutech.management.mapper;

import com.insutech.management.dto.ManagedPolicyRequest;
import com.insutech.management.dto.ManagedPolicyResponse;


import com.insutech.management.entity.ManagedPolicy;
import org.springframework.stereotype.Component;

@Component
public class ManagedPolicyMapper {

    public ManagedPolicy toEntity(ManagedPolicyRequest dto) {
        return ManagedPolicy.builder()
                .policyNumber(dto.getPolicyNumber())
                .type(dto.getType())
                .status(dto.getStatus())
                .assignedTo(dto.getAssignedTo())
                .build();
    }

    public ManagedPolicyResponse toResponse(ManagedPolicy entity) {
        return ManagedPolicyResponse.builder()
                .id(entity.getId())
                .policyNumber(entity.getPolicyNumber())
                .type(entity.getType())
                .status(entity.getStatus())
                .assignedTo(entity.getAssignedTo())
                .build();
    }
}
