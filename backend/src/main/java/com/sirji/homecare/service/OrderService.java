package com.sirji.homecare.service;

import com.sirji.homecare.domain.AdminUser;
import com.sirji.homecare.domain.HomecareOrder;
import com.sirji.homecare.domain.OrderItem;
import com.sirji.homecare.domain.OrderStatus;
import com.sirji.homecare.domain.Product;
import com.sirji.homecare.domain.ServiceBranch;
import com.sirji.homecare.domain.SiteSettings;
import com.sirji.homecare.repository.OrderRepository;
import com.sirji.homecare.repository.ProductRepository;
import com.sirji.homecare.repository.ServiceBranchRepository;
import com.sirji.homecare.repository.SiteSettingsRepository;
import com.sirji.homecare.web.dto.AdminOrderItemResponse;
import com.sirji.homecare.web.dto.AdminOrderResponse;
import com.sirji.homecare.web.dto.ApprovalRequest;
import com.sirji.homecare.web.dto.CreateOrderItemRequest;
import com.sirji.homecare.web.dto.CreateOrderRequest;
import com.sirji.homecare.web.dto.OrderResponse;
import com.sirji.homecare.web.error.InvalidOrderStateException;
import com.sirji.homecare.web.error.ResourceNotFoundException;
import com.sirji.homecare.web.error.ServiceAreaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private static final String BULK_THRESHOLD_KEY = "bulk.quantity.threshold";

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ServiceBranchRepository branchRepository;
    private final SiteSettingsRepository settingsRepository;
    private final DistanceCalculator distanceCalculator;
    private final int configuredBulkThreshold;

    public OrderService(
        OrderRepository orderRepository,
        ProductRepository productRepository,
        ServiceBranchRepository branchRepository,
        SiteSettingsRepository settingsRepository,
        DistanceCalculator distanceCalculator,
        @Value("${app.bulk.quantity-threshold:50}") int configuredBulkThreshold
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.branchRepository = branchRepository;
        this.settingsRepository = settingsRepository;
        this.distanceCalculator = distanceCalculator;
        this.configuredBulkThreshold = configuredBulkThreshold;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        ServiceBranch branch = findNearestEligibleBranch(request);
        int totalQuantity = request.items().stream().mapToInt(CreateOrderItemRequest::quantity).sum();

        HomecareOrder order = new HomecareOrder();
        order.setOrderNumber("SH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setCustomerName(request.customerName().trim());
        order.setCustomerPhone(request.customerPhone().trim());
        order.setDeliveryAddress(request.deliveryAddress().trim());
        order.setLatitude(request.latitude());
        order.setLongitude(request.longitude());
        order.setBranch(branch);
        order.setStatus(OrderStatus.PENDING_APPROVAL);
        order.setBulkOrder(totalQuantity >= getBulkThreshold());

        for (CreateOrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findByIdAndActiveTrue(itemRequest.productId())
                .orElseThrow(() -> new ResourceNotFoundException("One or more requested products are unavailable."));
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(product.getPrice());
            order.addItem(item);
        }

        return toOrderResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse track(String orderNumber, String phone) {
        HomecareOrder order = orderRepository.findByOrderNumberAndCustomerPhone(orderNumber, phone)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found for the supplied order number and phone."));
        return toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<AdminOrderResponse> listForAdmin(OrderStatus status) {
        List<HomecareOrder> orders = status == null
            ? orderRepository.findAllByOrderByCreatedAtDesc()
            : orderRepository.findByStatusOrderByCreatedAtDesc(status);
        return orders.stream().map(this::toAdminResponse).toList();
    }

    @Transactional
    public AdminOrderResponse review(UUID orderId, ApprovalRequest request, AdminUser reviewer) {
        HomecareOrder order = findOrderWithDetails(orderId);
        if (order.getStatus() != OrderStatus.PENDING_APPROVAL) {
            throw new InvalidOrderStateException("Only pending orders can be approved or rejected.");
        }
        order.setStatus(request.approved() ? OrderStatus.APPROVED : OrderStatus.REJECTED);
        order.setReviewedBy(reviewer);
        order.setAdminNotes(request.notes());
        return toAdminResponse(orderRepository.save(order));
    }

    @Transactional
    public AdminOrderResponse markFulfilled(UUID orderId) {
        HomecareOrder order = findOrderWithDetails(orderId);
        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new InvalidOrderStateException("Only approved orders can be marked fulfilled.");
        }
        order.setStatus(OrderStatus.FULFILLED);
        return toAdminResponse(orderRepository.save(order));
    }

    private ServiceBranch findNearestEligibleBranch(CreateOrderRequest request) {
        return branchRepository.findByActiveTrueOrderByPriorityDesc().stream()
            .map(branch -> new BranchDistance(branch, distanceCalculator.distanceKm(
                request.latitude().doubleValue(), request.longitude().doubleValue(),
                branch.getLatitude().doubleValue(), branch.getLongitude().doubleValue())))
            .filter(candidate -> candidate.distanceKm() <= candidate.branch().getRadiusKm().doubleValue())
            .min(Comparator.comparingDouble(BranchDistance::distanceKm)
                .thenComparing(Comparator.comparingInt((BranchDistance candidate) -> candidate.branch().getPriority()).reversed()))
            .map(BranchDistance::branch)
            .orElseThrow(ServiceAreaException::new);
    }

    private int getBulkThreshold() {
        return settingsRepository.findBySettingKey(BULK_THRESHOLD_KEY)
            .map(SiteSettings::getSettingValue)
            .map(this::parsePositiveThreshold)
            .orElse(Math.max(1, configuredBulkThreshold));
    }

    private int parsePositiveThreshold(String value) {
        try {
            int threshold = Integer.parseInt(value);
            return threshold > 0 ? threshold : Math.max(1, configuredBulkThreshold);
        } catch (NumberFormatException exception) {
            return Math.max(1, configuredBulkThreshold);
        }
    }

    private HomecareOrder findOrderWithDetails(UUID orderId) {
        return orderRepository.findWithDetailsById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found."));
    }

    private OrderResponse toOrderResponse(HomecareOrder order) {
        return new OrderResponse(order.getId(), order.getOrderNumber(), order.getStatus(), order.isBulkOrder(),
            order.getBranch().getName(), order.getCreatedAt());
    }

    private AdminOrderResponse toAdminResponse(HomecareOrder order) {
        List<AdminOrderItemResponse> items = order.getItems().stream()
            .map(item -> new AdminOrderItemResponse(item.getProduct().getId(), item.getProduct().getName(),
                item.getQuantity(), item.getUnitPrice()))
            .toList();
        return new AdminOrderResponse(order.getId(), order.getOrderNumber(), order.getCustomerName(),
            order.getCustomerPhone(), order.getDeliveryAddress(), order.getStatus(), order.isBulkOrder(),
            order.getBranch().getName(), order.getAdminNotes(), order.getCreatedAt(), items);
    }

    private record BranchDistance(ServiceBranch branch, double distanceKm) {}
}
