package com.example.itq.mapper;

import com.example.itq.dto.DocumentResponse;
import com.example.itq.model.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentResponse toDocumentResponse(Document document) {
        DocumentResponse response = new DocumentResponse();
        response.setId(document.getId());
        response.setUniqueNumber(document.getUniqueNumber());
        response.setAuthor(document.getAuthor());
        response.setTitle(document.getTitle());
        response.setStatus(document.getStatus());
        response.setCreatedAt(document.getCreatedAt());
        response.setUpdatedAt(document.getUpdatedAt());
        return response;
    }
}
