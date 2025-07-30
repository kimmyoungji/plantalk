package com.plantalk.chat.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plant_states")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "state_id")
    private Long stateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @Column(name = "light_level")
    private Integer lightLevel;

    @Column(name = "temperature")
    private Float temperature;

    @Column(name = "moisture")
    private Integer moisture;

    @Column(name = "touched")
    private Boolean touched;

    @Column(name = "measured_at")
    private LocalDateTime measuredAt;

    @OneToMany(mappedBy = "state", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        measuredAt = LocalDateTime.now();
    }
}
