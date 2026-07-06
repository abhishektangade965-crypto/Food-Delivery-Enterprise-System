package com.delivo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "delivery_agents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAgent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String vehicleNumber;
    private String vehicleType; // BICYCLE, MOTORCYCLE, CAR
    private String status; // ACTIVE, DELIVERING, OFFLINE
    private double currentLatitude;
    private double currentLongitude;
}
