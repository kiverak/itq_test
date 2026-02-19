package com.example.itq.dto;

import lombok.Data;

@Data
public class CreateDocumentRequest {
    private String author;
    private String title;
    private String initiator;
}
