package com.example.itq.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ApprovalRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    private LocalDateTime approvalDate;

    @PrePersist
    protected void onCreate() {
        this.approvalDate = LocalDateTime.now();
    }
}
