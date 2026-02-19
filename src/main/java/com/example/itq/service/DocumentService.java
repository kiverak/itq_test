package com.example.itq.service;

import com.example.itq.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface DocumentService {
    DocumentResponse createDocument(CreateDocumentRequest request);
    Optional<DocumentWithHistoryResponse> getDocumentWithHistory(Long id);
    List<DocumentResponse> getDocumentsByIds(List<Long> ids);
    Page<DocumentResponse> getAllDocuments(Pageable pageable);
    List<TransitionResult> submitDocumentsForApproval(TransitionRequest request);
    List<TransitionResult> approveDocuments(TransitionRequest request);
}
