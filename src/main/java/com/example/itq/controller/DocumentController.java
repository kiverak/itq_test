package com.example.itq.controller;

import com.example.itq.dto.*;
import com.example.itq.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(@RequestBody CreateDocumentRequest request) {
        return ResponseEntity.ok(documentService.createDocument(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentWithHistoryResponse> getDocument(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocumentWithHistory(id));
    }

    @GetMapping
    public ResponseEntity<Page<DocumentResponse>> getDocuments(Pageable pageable) {
        return ResponseEntity.ok(documentService.getAllDocuments(pageable));
    }

    @PostMapping("/batch")
    public ResponseEntity<Page<DocumentResponse>> getDocumentsByIds(
            @RequestBody BatchDocumentRequest request,
            @PageableDefault(size = 10, sort = "uniqueNumber", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(documentService.getDocumentsByIds(request, pageable));
    }

    @PostMapping("/submit")
    public ResponseEntity<List<TransitionResult>> submitForApproval(@Valid @RequestBody TransitionRequest request) {
        return ResponseEntity.ok(documentService.submitDocumentsForApproval(request));
    }

    @PostMapping("/approve")
    public ResponseEntity<List<TransitionResult>> approve(@Valid @RequestBody TransitionRequest request) {
        return ResponseEntity.ok(documentService.approveDocuments(request));
    }
}
