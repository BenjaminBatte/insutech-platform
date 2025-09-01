package com.insutech.management.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ManagedPolicyRequest {
    private String policyNumber;
    private String type;
    private String status;
    private String assignedTo;
}