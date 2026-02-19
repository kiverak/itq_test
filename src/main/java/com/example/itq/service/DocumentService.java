package com.example.itq.service;

import com.example.itq.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DocumentService {
    DocumentResponse createDocument(CreateDocumentRequest request);
    DocumentWithHistoryResponse getDocumentWithHistory(Long id);
    Page<DocumentResponse> getDocumentsByIds(BatchDocumentRequest request, Pageable pageable);
    Page<DocumentResponse> getAllDocuments(Pageable pageable);
    List<TransitionResult> submitDocumentsForApproval(TransitionRequest request);
    List<TransitionResult> approveDocuments(TransitionRequest request);
}
