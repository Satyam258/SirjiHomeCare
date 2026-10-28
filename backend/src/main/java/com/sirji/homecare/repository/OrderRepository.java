package com.sirji.homecare.repository;

import com.sirji.homecare.domain.HomecareOrder;
import com.sirji.homecare.domain.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<HomecareOrder, UUID> {
    @EntityGraph(attributePaths = {"branch", "items", "items.product"})
    List<HomecareOrder> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"branch", "items", "items.product"})
    List<HomecareOrder> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    @EntityGraph(attributePaths = {"branch", "items", "items.product"})
    Optional<HomecareOrder> findByOrderNumberAndCustomerPhone(String orderNumber, String customerPhone);

    @EntityGraph(attributePaths = {"branch", "items", "items.product"})
    Optional<HomecareOrder> findWithDetailsById(UUID id);
}
