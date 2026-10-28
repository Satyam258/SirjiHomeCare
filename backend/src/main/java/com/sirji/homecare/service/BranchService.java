package com.sirji.homecare.service;

import com.sirji.homecare.domain.ServiceBranch;
import com.sirji.homecare.repository.ServiceBranchRepository;
import com.sirji.homecare.web.dto.BranchResponse;
import com.sirji.homecare.web.dto.BranchUpsertRequest;
import com.sirji.homecare.web.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BranchService {
    private final ServiceBranchRepository branchRepository;

    public BranchService(ServiceBranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> list() {
        return branchRepository.findAllByOrderByPriorityDesc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public BranchResponse create(BranchUpsertRequest request) {
        ServiceBranch branch = new ServiceBranch();
        apply(branch, request);
        return toResponse(branchRepository.save(branch));
    }

    @Transactional
    public BranchResponse update(UUID id, BranchUpsertRequest request) {
        ServiceBranch branch = branchRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Service branch not found."));
        apply(branch, request);
        return toResponse(branchRepository.save(branch));
    }

    private void apply(ServiceBranch branch, BranchUpsertRequest request) {
        branch.setName(request.name().trim());
        branch.setAddress(request.address().trim());
        branch.setLatitude(request.latitude());
        branch.setLongitude(request.longitude());
        branch.setRadiusKm(request.radiusKm());
        branch.setPriority(request.priority());
        branch.setActive(request.active());
    }

    private BranchResponse toResponse(ServiceBranch branch) {
        return new BranchResponse(branch.getId(), branch.getName(), branch.getAddress(), branch.getLatitude(),
            branch.getLongitude(), branch.getRadiusKm(), branch.getPriority(), branch.isActive());
    }
}
