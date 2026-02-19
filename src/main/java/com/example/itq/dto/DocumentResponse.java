package com.example.itq.dto;

import com.example.itq.model.DocumentStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentResponse {
    private Long id;
    private String uniqueNumber;
    private String author;
    private String title;
    private DocumentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
