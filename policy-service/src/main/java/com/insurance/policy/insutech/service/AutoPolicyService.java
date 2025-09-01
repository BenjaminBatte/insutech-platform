package com.insurance.policy.insutech.service;

import com.insurance.policy.insutech.dto.AutoPolicyDTO;
import com.insurance.policy.insutech.model.AutoPolicyType;
import com.insurance.policy.insutech.model.PolicyStatus;

import java.time.LocalDate;
import java.util.List;

public interface AutoPolicyService {

  AutoPolicyDTO getPolicyByPolicyNumber(String policyNumber);

  AutoPolicyDTO createPolicy(AutoPolicyDTO autoPolicyDTO);

  AutoPolicyDTO getPolicyById(Long id);

  List<AutoPolicyDTO> getAllPolicies();

  AutoPolicyDTO updatePolicy(Long id, AutoPolicyDTO autoPolicyDTO);

  void deletePolicy(Long id);

  List<AutoPolicyDTO> createPolicies(List<AutoPolicyDTO> autoPolicyDTOs);

  /**
   * Get all policies with advanced filtering.
   *
   * @param startDate   filter by policy start date (>=)
   * @param endDate     filter by policy end date (<=)
   * @param status      filter by policy status
   * @param type        filter by policy type
   * @param vehicleMake filter by vehicle make
   * @param firstName   filter by policy holder first name
   * @param lastName    filter by policy holder last name
   * @param minPremium  filter by minimum premium
   * @param maxPremium  filter by maximum premium
   * @param userId      filter by assigned user (nullable, optional)
   * @return list of matching policies
   */
  List<AutoPolicyDTO> getAllPolicies(LocalDate startDate,
                                     LocalDate endDate,
                                     PolicyStatus status,
                                     AutoPolicyType type,
                                     String vehicleMake,
                                     String firstName,
                                     String lastName,
                                     Double minPremium,
                                     Double maxPremium,
                                     Long userId);
}
