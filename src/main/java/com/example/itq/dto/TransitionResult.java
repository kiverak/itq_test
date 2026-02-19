package com.example.itq.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransitionResult {
    private Long documentId;
    private TransitionStatus status;
    private String message;
}
