package com.insutech.management.dto;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ManagedPolicyResponse {
    private Long id;
    private String policyNumber;
    private String type;
    private String status;
    private String assignedTo;
}