package com.insutech.management.exception;

public class ManagedPolicyNotFoundException extends RuntimeException {
    public ManagedPolicyNotFoundException(Long id) {
        super("Policy not found with id: " + id);
    }
}
