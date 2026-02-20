package com.example.itq.service;

import com.example.itq.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface DocumentService {
    DocumentResponse createDocument(CreateDocumentRequest request);
    DocumentWithHistoryResponse getDocumentWithHistory(Long id);
    Page<DocumentResponse> getDocumentsByIds(BatchDocumentRequest request, Pageable pageable);
    Page<DocumentResponse> getAllDocuments(Pageable pageable);
    List<TransitionResult> submitDocumentsForApproval(TransitionRequest request);
    List<TransitionResult> approveDocuments(TransitionRequest request);
    ParallelApproveSummary parallelApprove(@Valid TransitionRequest request, int threads, int attempts);
    Page<DocumentResponse> search(String author, String status, Instant from, Instant to, Pageable pageable);
}
