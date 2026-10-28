package com.sirji.homecare.repository;

import com.sirji.homecare.domain.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {
    List<Feedback> findByApprovedTrueOrderByCreatedAtDesc();
    List<Feedback> findAllByOrderByCreatedAtDesc();
}
