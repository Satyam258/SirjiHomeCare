package com.sirji.homecare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "feedback")
public class Feedback extends AuditedEntity {
    @Column(name = "customer_name", nullable = false, length = 160)
    private String customerName;

    @Column(length = 254)
    private String email;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(nullable = false)
    private boolean approved;
}
