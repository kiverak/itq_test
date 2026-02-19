package com.example.itq.service;

import com.example.itq.dto.*;
import com.example.itq.exception.ResourceNotFoundException;
import com.example.itq.mapper.DocumentHistoryMapper;
import com.example.itq.mapper.DocumentMapper;
import com.example.itq.model.Document;
import com.example.itq.model.DocumentAction;
import com.example.itq.model.DocumentHistory;
import com.example.itq.model.DocumentStatus;
import com.example.itq.repository.DocumentHistoryRepository;
import com.example.itq.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentHistoryRepository documentHistoryRepository;
    private final DocumentMapper documentMapper;
    private final DocumentHistoryMapper documentHistoryMapper;
    private final DocumentApprovingService documentApprovingService;

    @Override
    @Transactional
    public DocumentResponse createDocument(CreateDocumentRequest request) {
        Document document = new Document();
        document.setAuthor(request.getAuthor());
        document.setTitle(request.getTitle());
        Document savedDocument = documentRepository.save(document);

        return documentMapper.toDocumentResponse(savedDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentWithHistoryResponse getDocumentWithHistory(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id " + id));

        List<DocumentHistory> history = documentHistoryRepository.findByDocumentId(id);

        DocumentWithHistoryResponse response = new DocumentWithHistoryResponse();
        response.setDocument(documentMapper.toDocumentResponse(document));
        response.setHistory(history.stream()
                .map(documentHistoryMapper::toDocumentHistoryResponse)
                .collect(Collectors.toList()));

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentResponse> getAllDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable)
                .map(documentMapper::toDocumentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentResponse> getDocumentsByIds(BatchDocumentRequest request, Pageable pageable) {
        return documentRepository.findByIdIn(request.getIds(), pageable)
                .map(documentMapper::toDocumentResponse);
    }

    @Override
    @Transactional
    public List<TransitionResult> submitDocumentsForApproval(TransitionRequest request) {
        List<Document> docs = documentRepository.findAllById(request.getDocumentIds());

        Map<Long, Document> docMap = new HashMap<>();
        for (Document doc : docs) {
            docMap.put(doc.getId(), doc);
        }

        for (Long id : request.getDocumentIds()) {
            docMap.putIfAbsent(id, null);
        }

        List<TransitionResult> results = new ArrayList<>();

        for (Long id : request.getDocumentIds()) {
            Document doc = docMap.get(id);

            TransitionResult result = new TransitionResult();
            result.setDocumentId(id);

            if (doc == null) {
                result.setStatus(TransitionStatus.NOT_FOUND);
            } else if (doc.getStatus() == DocumentStatus.SUBMITTED || doc.getStatus() == DocumentStatus.APPROVED) {
                result.setStatus(TransitionStatus.CONFLICT);
            } else {
                doc.setStatus(DocumentStatus.SUBMITTED);
                result.setStatus(TransitionStatus.SUCCESS);

                DocumentHistory history = new DocumentHistory();
                history.setDocument(doc);
                history.setAction(DocumentAction.SUBMIT);
                history.setInitiator(request.getInitiator());
                history.setComment(request.getComment());
                documentHistoryRepository.save(history);
            }

            results.add(result);
        }

        return results;
    }

    @Override
    public List<TransitionResult> approveDocuments(TransitionRequest request) {
        List<Document> docs = documentRepository.findAllById(request.getDocumentIds());

        Map<Long, Document> docMap = new HashMap<>();
        for (Document doc : docs) {
            docMap.put(doc.getId(), doc);
        }

        for (Long id : request.getDocumentIds()) {
            docMap.putIfAbsent(id, null);
        }

        List<TransitionResult> results = new ArrayList<>();

        for (Long id : request.getDocumentIds()) {
            Document doc = docMap.get(id);
            TransitionResult result = new TransitionResult();
            try {
                result = documentApprovingService.approveDocument(id, doc, request.getInitiator(), request.getComment());
            } catch (Exception e) {
                result.setDocumentId(id);
                result.setStatus(TransitionStatus.REGISTRY_ERROR);
                // TODO add logging
            }
            results.add(result);
        }

        return results;
    }

}
