package com.insurance.policy.insutech.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.insurance.policy.insutech.dto.AutoPolicyDTO;
import com.insurance.policy.insutech.exception.AutoPolicyNotFoundException;
import com.insurance.policy.insutech.mapper.AutoPolicyMapper;
import com.insurance.policy.insutech.model.AutoPolicy;
import com.insurance.policy.insutech.model.AutoPolicyType;
import com.insurance.policy.insutech.model.PolicyStatus;
import com.insurance.policy.insutech.repository.AutoPolicyRepository;
import com.insurance.policy.insutech.service.AutoPolicyService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AutoPolicyServiceImpl implements AutoPolicyService {

    private final AutoPolicyRepository autoPolicyRepository;
    private final AutoPolicyMapper autoPolicyMapper = AutoPolicyMapper.INSTANCE;

    @PersistenceContext
    private EntityManager entityManager;

    // Manual cache for complex queries with dynamic filters
    private final Cache<String, List<AutoPolicyDTO>> filteredPoliciesCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(200)
            .build();

    /**
     * Get single policy by ID (cached in "policies" region).
     */
    @Override
    @Cacheable(value = "policies", key = "#id")
    public AutoPolicyDTO getPolicyById(Long id) {
        return autoPolicyRepository.findById(id)
                .map(autoPolicyMapper::toDTO)
                .orElseThrow(() ->
                        new AutoPolicyNotFoundException("AutoPolicy not found with ID: " + id));
    }

    /**
     * Get single policy by policy number (cached separately).
     */
    @Override
    @Cacheable(value = "policyNumbers", key = "#policyNumber")
    public AutoPolicyDTO getPolicyByPolicyNumber(String policyNumber) {
        AutoPolicy policy = autoPolicyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() ->
                        new AutoPolicyNotFoundException("AutoPolicy with number " + policyNumber + " not found"));
        return autoPolicyMapper.toDTO(policy);
    }

    /**
     * Create new policy, evict caches to avoid stale data.
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "allPolicies", allEntries = true),
            @CacheEvict(value = "filteredPolicies", allEntries = true),
            @CacheEvict(value = "policyNumbers", allEntries = true)
    })

    public AutoPolicyDTO createPolicy(AutoPolicyDTO autoPolicyDTO) {
        if (autoPolicyRepository.findByPolicyNumber(autoPolicyDTO.getPolicyNumber()).isPresent()) {
            throw new IllegalArgumentException("Policy number already exists: " + autoPolicyDTO.getPolicyNumber());
        }
        AutoPolicy policy = autoPolicyMapper.toEntity(autoPolicyDTO);
        return autoPolicyMapper.toDTO(autoPolicyRepository.save(policy));
    }


    /**
     * Get all policies (cached in "allPolicies").
     */
    @Override
    @Cacheable(value = "allPolicies", key = "'all'")
    public List<AutoPolicyDTO> getAllPolicies() {
        List<AutoPolicy> entities = autoPolicyRepository.findAll();
        if (entities.isEmpty()) {
            throw new AutoPolicyNotFoundException("No auto policies found.");
        }
        return entities.stream()
                .map(autoPolicyMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update policy by ID. Updates single cache entry, evicts lists.
     */
    @Override
    @Caching(
            put = @CachePut(value = "policies", key = "#id"),
            evict = {
                    @CacheEvict(value = "policyNumbers", allEntries = true),
                    @CacheEvict(value = "allPolicies", allEntries = true),
                    @CacheEvict(value = "filteredPolicies", allEntries = true)
            }
    )
    public AutoPolicyDTO updatePolicy(Long id, AutoPolicyDTO autoPolicyDTO) {
        if (!autoPolicyRepository.existsById(id)) {
            throw new AutoPolicyNotFoundException("AutoPolicy not found with ID: " + id);
        }
        AutoPolicy updated = autoPolicyMapper.toEntity(autoPolicyDTO);
        updated.setId(id); // ensure existing ID
        return autoPolicyMapper.toDTO(autoPolicyRepository.save(updated));
    }

    /**
     * Delete policy and evict all caches that may contain it.
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "policies", key = "#id"),
            @CacheEvict(value = "policyNumbers", allEntries = true),
            @CacheEvict(value = "allPolicies", allEntries = true),
            @CacheEvict(value = "filteredPolicies", allEntries = true)
    })
    public void deletePolicy(Long id) {
        if (!autoPolicyRepository.existsById(id)) {
            throw new AutoPolicyNotFoundException("AutoPolicy with ID " + id + " not found");
        }
        autoPolicyRepository.deleteById(id);
    }

    /**
     * Create policies in batch. Evict list caches since many policies may be added.
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "allPolicies", allEntries = true),
            @CacheEvict(value = "filteredPolicies", allEntries = true),
            @CacheEvict(value = "policyNumbers", allEntries = true)
    })
    public List<AutoPolicyDTO> createPolicies(List<AutoPolicyDTO> autoPolicyDTOs) {
        List<AutoPolicy> entities = autoPolicyDTOs.stream()
                .map(autoPolicyMapper::toEntity)
                .collect(Collectors.toList());
        return autoPolicyRepository.saveAll(entities).stream()
                .map(autoPolicyMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Complex filtered query with manual caching.
     * Supports filtering by userId in addition to other criteria.
     */
    @Override
    public List<AutoPolicyDTO> getAllPolicies(
            LocalDate startDate,
            LocalDate endDate,
            PolicyStatus status,
            AutoPolicyType type,
            String vehicleMake,
            String firstName,
            String lastName,
            Double minPremium,
            Double maxPremium,
            Long userId
    ) {
        String cacheKey = generateCacheKey(startDate, endDate, status, type,
                vehicleMake, firstName, lastName, minPremium, maxPremium, userId);

        List<AutoPolicyDTO> cached = filteredPoliciesCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<AutoPolicyDTO> result = executeFilteredQuery(startDate, endDate, status, type,
                vehicleMake, firstName, lastName, minPremium, maxPremium, userId);

        filteredPoliciesCache.put(cacheKey, result);
        return result;
    }

    private List<AutoPolicyDTO> executeFilteredQuery(
            LocalDate startDate,
            LocalDate endDate,
            PolicyStatus status,
            AutoPolicyType type,
            String vehicleMake,
            String firstName,
            String lastName,
            Double minPremium,
            Double maxPremium,
            Long userId
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AutoPolicy> query = cb.createQuery(AutoPolicy.class);
        Root<AutoPolicy> root = query.from(AutoPolicy.class);

        List<Predicate> predicates = new ArrayList<>();

        if (startDate != null) predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), startDate));
        if (endDate != null) predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), endDate));
        if (status != null) predicates.add(cb.equal(root.get("status"), status));
        if (type != null) predicates.add(cb.equal(root.get("policyType"), type));
        if (vehicleMake != null && !vehicleMake.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("vehicleMake")), "%" + vehicleMake.toLowerCase() + "%"));
        }
        if (firstName != null && !firstName.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
        }
        if (lastName != null && !lastName.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
        }
        if (minPremium != null) predicates.add(cb.greaterThanOrEqualTo(root.get("premiumAmount"), minPremium));
        if (maxPremium != null) predicates.add(cb.lessThanOrEqualTo(root.get("premiumAmount"), maxPremium));
        if (userId != null) predicates.add(cb.equal(root.get("userId"), userId)); // ✅ filter by user

        query.select(root).where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(query).getResultList().stream()
                .map(autoPolicyMapper::toDTO)
                .collect(Collectors.toList());
    }

    private String generateCacheKey(
            LocalDate startDate,
            LocalDate endDate,
            PolicyStatus status,
            AutoPolicyType type,
            String vehicleMake,
            String firstName,
            String lastName,
            Double minPremium,
            Double maxPremium,
            Long userId
    ) {
        return String.format("filter_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s",
                startDate != null ? startDate : "null",
                endDate != null ? endDate : "null",
                status != null ? status : "null",
                type != null ? type : "null",
                vehicleMake != null ? vehicleMake : "null",
                firstName != null ? firstName : "null",
                lastName != null ? lastName : "null",
                minPremium != null ? minPremium : "null",
                maxPremium != null ? maxPremium : "null",
                userId != null ? userId : "null");
    }

    /** Clear all cached filter queries (for admin/debugging). */
    public void clearFilteredCache() {
        filteredPoliciesCache.invalidateAll();
    }
}
