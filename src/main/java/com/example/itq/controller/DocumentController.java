package com.example.itq.controller;

import com.example.itq.dto.*;
import com.example.itq.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        return documentService.getDocumentWithHistory(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<DocumentResponse>> getDocuments(Pageable pageable) {
        return ResponseEntity.ok(documentService.getAllDocuments(pageable));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByIds(@RequestBody BatchDocumentRequest request) {
        return ResponseEntity.ok(documentService.getDocumentsByIds(request.getIds()));
    }

    @PostMapping("/submit")
    public ResponseEntity<List<TransitionResult>> submitForApproval(@RequestBody TransitionRequest request) {
        return ResponseEntity.ok(documentService.submitDocumentsForApproval(request));
    }

    @PostMapping("/approve")
    public ResponseEntity<List<TransitionResult>> approve(@RequestBody TransitionRequest request) {
        return ResponseEntity.ok(documentService.approveDocuments(request));
    }
}
