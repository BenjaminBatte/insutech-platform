package com.insutech.management.controller;


import com.insutech.management.dto.ManagedPolicyRequest;
import com.insutech.management.dto.ManagedPolicyResponse;
import com.insutech.management.service.ManagedPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/managed-policies")
@RequiredArgsConstructor
public class ManagedPolicyController {

    private final ManagedPolicyService service;

    @PostMapping
    public ResponseEntity<ManagedPolicyResponse> createPolicy(@RequestBody ManagedPolicyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPolicy(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManagedPolicyResponse> getPolicyById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPolicyById(id));
    }

    @GetMapping
    public ResponseEntity<List<ManagedPolicyResponse>> getPolicies(
            @RequestParam String role,
            @RequestParam(required = false) String username) {
        return ResponseEntity.ok(service.getPolicies(role, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePolicy(@PathVariable Long id) {
        service.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }
}
