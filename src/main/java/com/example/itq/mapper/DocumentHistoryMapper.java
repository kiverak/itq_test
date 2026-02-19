package com.example.itq.mapper;

import com.example.itq.dto.DocumentHistoryResponse;
import com.example.itq.model.DocumentHistory;
import org.springframework.stereotype.Component;

@Component
public class DocumentHistoryMapper {

    public DocumentHistoryResponse toDocumentHistoryResponse(DocumentHistory documentHistory) {
        DocumentHistoryResponse response = new DocumentHistoryResponse();
        response.setId(documentHistory.getId());
        response.setInitiator(documentHistory.getInitiator());
        response.setTimestamp(documentHistory.getTimestamp());
        response.setAction(documentHistory.getAction());
        response.setComment(documentHistory.getComment());
        return response;
    }
}
