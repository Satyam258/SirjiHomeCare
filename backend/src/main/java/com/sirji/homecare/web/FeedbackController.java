package com.sirji.homecare.web;

import com.sirji.homecare.service.FeedbackService;
import com.sirji.homecare.web.dto.FeedbackRequest;
import com.sirji.homecare.web.dto.FeedbackResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackResponse submit(@Valid @RequestBody FeedbackRequest request) {
        return feedbackService.submit(request);
    }

    @GetMapping
    public List<FeedbackResponse> listApproved() {
        return feedbackService.listPublic();
    }
}
