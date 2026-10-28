package com.sirji.homecare.service;

import com.sirji.homecare.domain.Feedback;
import com.sirji.homecare.repository.FeedbackRepository;
import com.sirji.homecare.web.dto.FeedbackRequest;
import com.sirji.homecare.web.dto.FeedbackResponse;
import com.sirji.homecare.web.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @Transactional
    public FeedbackResponse submit(FeedbackRequest request) {
        Feedback feedback = new Feedback();
        feedback.setCustomerName(request.customerName().trim());
        feedback.setEmail(request.email());
        feedback.setRating(request.rating());
        feedback.setMessage(request.message().trim());
        feedback.setApproved(false);
        return toResponse(feedbackRepository.save(feedback));
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> listPublic() {
        return feedbackRepository.findByApprovedTrueOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> listForAdmin() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public FeedbackResponse setApproval(UUID id, boolean approved) {
        Feedback feedback = feedbackRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Feedback not found."));
        feedback.setApproved(approved);
        return toResponse(feedbackRepository.save(feedback));
    }

    private FeedbackResponse toResponse(Feedback feedback) {
        return new FeedbackResponse(feedback.getId(), feedback.getCustomerName(), feedback.getRating(),
            feedback.getMessage(), feedback.getCreatedAt());
    }
}
