package com.insutech.management.repository;


import com.insutech.management.entity.ManagedPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManagedPolicyRepository extends JpaRepository<ManagedPolicy, Long> {
    List<ManagedPolicy> findByAssignedTo(String username);
}
