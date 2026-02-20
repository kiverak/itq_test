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
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j2
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt",
            "updatedAt",
            "author",
            "status"
    );

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
    public List<TransitionResult> submitDocuments(TransitionRequest request) {
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
            TransitionResult result = new TransitionResult();
            try {
                result = documentApprovingService.approveDocument(id, request.getInitiator(), request.getComment());
            } catch (Exception e) {
                result.setDocumentId(id);
                result.setStatus(TransitionStatus.REGISTRY_ERROR);
                log.warn("Error processing document approval for ID {}: {}", id, e.getMessage(), e);
            }
            results.add(result);
        }

        return results;
    }

    @Override
    public ParallelApproveSummary parallelApprove(TransitionRequest request, int threads, int attempts) {

        if (threads < 1 || attempts < 1) {
            throw new IllegalArgumentException("threads and attempts must be greater than 0");
        }
        if (request.getDocumentIds().isEmpty()) {
            throw new IllegalArgumentException("documentIds must not be empty");
        }
        Long id = request.getDocumentIds().getFirst();

        List<Future<TransitionResult>> futures = new ArrayList<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            for (int t = 0; t < threads; t++) {
                futures.addAll(
                        IntStream.range(0, attempts)
                                .mapToObj(i -> executor.submit(() -> {
                                    try {
                                        return documentApprovingService.approveDocument(
                                                id,
                                                request.getInitiator(),
                                                request.getComment()
                                        );
                                    } catch (Exception e) {
                                        TransitionResult r = new TransitionResult();
                                        r.setDocumentId(id);
                                        r.setStatus(TransitionStatus.REGISTRY_ERROR);
                                        return r;
                                    }
                                }))
                                .toList()
                );
            }
        }

        int success = 0;
        int conflict = 0;
        int registryError = 0;
        int notFound = 0;

        for (Future<TransitionResult> f : futures) {
            try {
                TransitionResult r = f.get();
                switch (r.getStatus()) {
                    case SUCCESS -> success++;
                    case CONFLICT -> conflict++;
                    case REGISTRY_ERROR -> registryError++;
                    case NOT_FOUND -> notFound++;
                }
            } catch (Exception e) {
                registryError++;
            }
        }

        Document finalDoc = documentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Document not found with id " + id));

        return new ParallelApproveSummary(success, conflict + notFound, registryError, finalDoc.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentResponse> search(String author,
                                         String status,
                                         Instant from,
                                         Instant to,
                                         Pageable pageable) {

        Sort validatedSort = validateSort(pageable.getSort());
        Pageable validatedPageable = PageRequest.of(
                Math.max(pageable.getPageNumber(), 0),
                pageable.getPageSize() < 1 ? 10 : pageable.getPageSize(),
                validatedSort
        );

        Specification<Document> spec = (root, query, cb) -> cb.conjunction();

        if (author != null && !author.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("author"), author));
        }

        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), DocumentStatus.valueOf(status)));
        }

        if (from != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }

        if (to != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }

        Page<Document> page = documentRepository.findAll(spec, validatedPageable);

        return page.map(documentMapper::toDocumentResponse);
    }

    private Sort validateSort(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return Sort.unsorted();
        }

        List<Sort.Order> validOrders = new ArrayList<>();

        for (Sort.Order order : sort) {
            if (ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
                validOrders.add(order);
            } else {
                log.warn("Ignoring unsupported sort field: {}", order.getProperty());
            }
        }

        return validOrders.isEmpty() ? Sort.unsorted() : Sort.by(validOrders);
    }

}
