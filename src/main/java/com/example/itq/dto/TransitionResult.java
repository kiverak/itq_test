package com.example.itq.dto;

import lombok.Data;

@Data
public class TransitionResult {
    private Long documentId;
    private TransitionStatus status;
}
