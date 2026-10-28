package com.sirji.homecare.repository;

import com.sirji.homecare.domain.ServiceBranch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServiceBranchRepository extends JpaRepository<ServiceBranch, UUID> {
    List<ServiceBranch> findByActiveTrueOrderByPriorityDesc();
    List<ServiceBranch> findAllByOrderByPriorityDesc();
}
