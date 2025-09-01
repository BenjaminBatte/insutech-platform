package com.insutech.management.service;



import com.insutech.management.dto.ManagedPolicyRequest;
import com.insutech.management.dto.ManagedPolicyResponse;

import java.util.List;

public interface ManagedPolicyService {
    ManagedPolicyResponse createPolicy(ManagedPolicyRequest request);
    ManagedPolicyResponse getPolicyById(Long id);
    List<ManagedPolicyResponse> getPolicies(String role, String username);
    void deletePolicy(Long id);
}
