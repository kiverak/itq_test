package com.example.itq.dto;

import com.example.itq.model.DocumentAction;
import lombok.Data;

import java.time.Instant;

@Data
public class DocumentHistoryResponse {
    private Long id;
    private String initiator;
    private Instant timestamp;
    private DocumentAction action;
    private String comment;
}
