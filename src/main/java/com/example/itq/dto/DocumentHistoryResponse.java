package com.example.itq.dto;

import com.example.itq.model.DocumentAction;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentHistoryResponse {
    private Long id;
    private String initiator;
    private LocalDateTime timestamp;
    private DocumentAction action;
    private String comment;
}
