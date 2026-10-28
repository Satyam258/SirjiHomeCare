package com.sirji.homecare.service;

import com.sirji.homecare.domain.HomecareOrder;
import com.sirji.homecare.domain.OrderStatus;
import com.sirji.homecare.domain.Product;
import com.sirji.homecare.domain.ServiceBranch;
import com.sirji.homecare.domain.SiteSettings;
import com.sirji.homecare.repository.OrderRepository;
import com.sirji.homecare.repository.ProductRepository;
import com.sirji.homecare.repository.ServiceBranchRepository;
import com.sirji.homecare.repository.SiteSettingsRepository;
import com.sirji.homecare.web.dto.CreateOrderItemRequest;
import com.sirji.homecare.web.dto.CreateOrderRequest;
import com.sirji.homecare.web.error.ServiceAreaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    private static final UUID PRODUCT_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ServiceBranchRepository branchRepository;
    @Mock private SiteSettingsRepository settingsRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, productRepository, branchRepository, settingsRepository,
            new DistanceCalculator(), 50);
    }

    @Test
    void marksQuantityThresholdOrdersBulkAndRequiresApproval() {
        ServiceBranch branch = branchAt("12.971600", "77.594600", "25.00");
        Product product = new Product();
        product.setPrice(new BigDecimal("249.00"));
        SiteSettings threshold = new SiteSettings();
        threshold.setSettingValue("2");
        when(branchRepository.findByActiveTrueOrderByPriorityDesc()).thenReturn(List.of(branch));
        when(settingsRepository.findBySettingKey("bulk.quantity.threshold")).thenReturn(Optional.of(threshold));
        when(productRepository.findByIdAndActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(HomecareOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.create(request(12.9716, 77.5946, 2));

        ArgumentCaptor<HomecareOrder> savedOrder = ArgumentCaptor.forClass(HomecareOrder.class);
        verify(orderRepository).save(savedOrder.capture());
        assertEquals(OrderStatus.PENDING_APPROVAL, savedOrder.getValue().getStatus());
        assertTrue(savedOrder.getValue().isBulkOrder());
        assertEquals(1, savedOrder.getValue().getItems().size());
    }

    @Test
    void rejectsOrdersOutsideEveryActiveBranchRadius() {
        when(branchRepository.findByActiveTrueOrderByPriorityDesc())
            .thenReturn(List.of(branchAt("12.971600", "77.594600", "1.00")));

        assertThrows(ServiceAreaException.class, () -> orderService.create(request(13.5, 77.5946, 1)));
        verify(productRepository, never()).findByIdAndActiveTrue(any());
    }

    @Test
    void doesNotMarkBelowThresholdQuantitiesAsBulk() {
        SiteSettings threshold = new SiteSettings();
        threshold.setSettingValue("50");
        when(branchRepository.findByActiveTrueOrderByPriorityDesc())
            .thenReturn(List.of(branchAt("12.971600", "77.594600", "25.00")));
        when(settingsRepository.findBySettingKey("bulk.quantity.threshold")).thenReturn(Optional.of(threshold));
        when(productRepository.findByIdAndActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(new Product()));
        when(orderRepository.save(any(HomecareOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.create(request(12.9716, 77.5946, 1));

        ArgumentCaptor<HomecareOrder> savedOrder = ArgumentCaptor.forClass(HomecareOrder.class);
        verify(orderRepository).save(savedOrder.capture());
        assertFalse(savedOrder.getValue().isBulkOrder());
    }

    private ServiceBranch branchAt(String latitude, String longitude, String radius) {
        ServiceBranch branch = new ServiceBranch();
        branch.setName("Test branch");
        branch.setLatitude(new BigDecimal(latitude));
        branch.setLongitude(new BigDecimal(longitude));
        branch.setRadiusKm(new BigDecimal(radius));
        return branch;
    }

    private CreateOrderRequest request(double latitude, double longitude, int quantity) {
        return new CreateOrderRequest("Customer", "+91 9876543210", "Test address",
            BigDecimal.valueOf(latitude), BigDecimal.valueOf(longitude),
            List.of(new CreateOrderItemRequest(PRODUCT_ID, quantity)));
    }
}
