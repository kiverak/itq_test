package com.example.itq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransitionResult {
    private Long documentId;
    private TransitionStatus status;
    private String message; // Optional message for more details
}
