package com.sirji.homecare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "service_branches")
public class ServiceBranch extends AuditedEntity {
    @Column(nullable = false, length = 140)
    private String name;

    @Column(nullable = false, columnDefinition = "text")
    private String address;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "radius_km", nullable = false, precision = 8, scale = 2)
    private BigDecimal radiusKm;

    @Column(nullable = false)
    private int priority;

    @Column(nullable = false)
    private boolean active = true;
}
