package com.example.itq.dto;

import lombok.Data;
import java.util.List;

@Data
public class DocumentWithHistoryResponse {
    private DocumentResponse document;
    private List<DocumentHistoryResponse> history;
}
