package com.sirji.homecare.web;

import com.sirji.homecare.domain.AdminUser;
import com.sirji.homecare.domain.OrderStatus;
import com.sirji.homecare.service.BranchService;
import com.sirji.homecare.service.FeedbackService;
import com.sirji.homecare.service.OrderService;
import com.sirji.homecare.service.SiteSettingsService;
import com.sirji.homecare.web.dto.AdminOrderResponse;
import com.sirji.homecare.web.dto.ApprovalRequest;
import com.sirji.homecare.web.dto.BranchResponse;
import com.sirji.homecare.web.dto.BranchUpsertRequest;
import com.sirji.homecare.web.dto.FeedbackResponse;
import com.sirji.homecare.web.dto.SettingUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final OrderService orderService;
    private final SiteSettingsService settingsService;
    private final FeedbackService feedbackService;
    private final BranchService branchService;

    public AdminController(OrderService orderService, SiteSettingsService settingsService, FeedbackService feedbackService,
                           BranchService branchService) {
        this.orderService = orderService;
        this.settingsService = settingsService;
        this.feedbackService = feedbackService;
        this.branchService = branchService;
    }

    @GetMapping("/orders")
    public List<AdminOrderResponse> listOrders(@RequestParam(required = false) OrderStatus status) {
        return orderService.listForAdmin(status);
    }

    @PatchMapping("/orders/{id}/approval")
    public AdminOrderResponse reviewOrder(
        @PathVariable UUID id,
        @Valid @RequestBody ApprovalRequest request,
        @AuthenticationPrincipal AdminUser reviewer
    ) {
        return orderService.review(id, request, reviewer);
    }

    @PatchMapping("/orders/{id}/fulfillment")
    public AdminOrderResponse fulfillOrder(@PathVariable UUID id) {
        return orderService.markFulfilled(id);
    }

    @GetMapping("/settings")
    public Map<String, String> settings() {
        return settingsService.list();
    }

    @PatchMapping("/settings/{key}")
    public Map.Entry<String, String> updateSetting(@PathVariable String key, @Valid @RequestBody SettingUpdateRequest request) {
        return settingsService.update(key, request.value());
    }

    @GetMapping("/feedback")
    public List<FeedbackResponse> feedback() {
        return feedbackService.listForAdmin();
    }

    @PatchMapping("/feedback/{id}/approval")
    public FeedbackResponse approveFeedback(@PathVariable UUID id, @RequestParam boolean approved) {
        return feedbackService.setApproval(id, approved);
    }

    @GetMapping("/branches")
    public List<BranchResponse> branches() {
        return branchService.list();
    }

    @PostMapping("/branches")
    @ResponseStatus(HttpStatus.CREATED)
    public BranchResponse createBranch(@Valid @RequestBody BranchUpsertRequest request) {
        return branchService.create(request);
    }

    @PutMapping("/branches/{id}")
    public BranchResponse updateBranch(@PathVariable UUID id, @Valid @RequestBody BranchUpsertRequest request) {
        return branchService.update(id, request);
    }
}
