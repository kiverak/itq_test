package com.example.itq.service;

import com.example.itq.dto.*;
import com.example.itq.exception.ResourceNotFoundException;
import com.example.itq.mapper.DocumentHistoryMapper;
import com.example.itq.mapper.DocumentMapper;
import com.example.itq.model.Document;
import com.example.itq.model.DocumentHistory;
import com.example.itq.model.DocumentStatus;
import com.example.itq.repository.DocumentHistoryRepository;
import com.example.itq.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentHistoryRepository documentHistoryRepository;

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private DocumentHistoryMapper documentHistoryMapper;

    @Mock
    private DocumentApprovingService documentApprovingService;

    @InjectMocks
    private DocumentServiceImpl documentService;

    // ---------------------------------------------------------
    // getDocumentWithHistory
    // ---------------------------------------------------------

    @Test
    void getDocumentWithHistory_success() {
        Long id = 1L;

        Document document = new Document();
        document.setId(id);

        DocumentResponse mappedDoc = new DocumentResponse();
        mappedDoc.setId(id);

        DocumentHistory h1 = new DocumentHistory();
        h1.setId(10L);

        DocumentHistoryResponse mappedH1 = new DocumentHistoryResponse();
        mappedH1.setId(10L);

        when(documentRepository.findById(id)).thenReturn(Optional.of(document));
        when(documentHistoryRepository.findByDocumentId(id)).thenReturn(List.of(h1));
        when(documentMapper.toDocumentResponse(document)).thenReturn(mappedDoc);
        when(documentHistoryMapper.toDocumentHistoryResponse(h1)).thenReturn(mappedH1);

        DocumentWithHistoryResponse response = documentService.getDocumentWithHistory(id);

        assertNotNull(response);
        assertEquals(mappedDoc, response.getDocument());
        assertEquals(1, response.getHistory().size());
        assertEquals(mappedH1, response.getHistory().get(0));

        verify(documentRepository).findById(id);
        verify(documentHistoryRepository).findByDocumentId(id);
    }

    @Test
    void getDocumentWithHistory_notFound() {
        Long id = 1L;

        when(documentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> documentService.getDocumentWithHistory(id));

        verify(documentRepository).findById(id);
        verifyNoInteractions(documentHistoryRepository);
    }

    // ---------------------------------------------------------
    // getAllDocuments
    // ---------------------------------------------------------

    @Test
    void getAllDocuments_success() {
        Pageable pageable = PageRequest.of(0, 10);

        Document d1 = new Document();
        d1.setId(1L);

        DocumentResponse mapped = new DocumentResponse();
        mapped.setId(1L);

        Page<Document> page = new PageImpl<>(List.of(d1));

        when(documentRepository.findAll(pageable)).thenReturn(page);
        when(documentMapper.toDocumentResponse(d1)).thenReturn(mapped);

        Page<DocumentResponse> result = documentService.getAllDocuments(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));

        verify(documentRepository).findAll(pageable);
    }

    // ---------------------------------------------------------
    // getDocumentsByIds
    // ---------------------------------------------------------

    @Test
    void getDocumentsByIds_success() {
        Pageable pageable = PageRequest.of(0, 10);

        BatchDocumentRequest request = new BatchDocumentRequest();
        request.setIds(List.of(1L, 2L));

        Document d1 = new Document();
        d1.setId(1L);

        DocumentResponse mapped = new DocumentResponse();
        mapped.setId(1L);

        Page<Document> page = new PageImpl<>(List.of(d1));

        when(documentRepository.findByIdIn(request.getIds(), pageable)).thenReturn(page);
        when(documentMapper.toDocumentResponse(d1)).thenReturn(mapped);

        Page<DocumentResponse> result = documentService.getDocumentsByIds(request, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));

        verify(documentRepository).findByIdIn(request.getIds(), pageable);
    }

    // ---------------------------------------------------------
    // submitDocuments
    // ---------------------------------------------------------

    @Test
    void submitDocuments_success() {
        Long id = 1L;

        Document doc = new Document();
        doc.setId(id);
        doc.setStatus(DocumentStatus.DRAFT);

        TransitionRequest request = new TransitionRequest(
                List.of(id),
                "initiator",
                "comment"
        );

        when(documentRepository.findAllById(request.getDocumentIds()))
                .thenReturn(List.of(doc));

        List<TransitionResult> results = documentService.submitDocuments(request);

        assertEquals(1, results.size());
        TransitionResult r = results.get(0);

        assertEquals(id, r.getDocumentId());
        assertEquals(TransitionStatus.SUCCESS, r.getStatus());
        assertEquals(DocumentStatus.SUBMITTED, doc.getStatus());

        verify(documentHistoryRepository, times(1)).save(any(DocumentHistory.class));
    }

    @Test
    void submitDocuments_notFound() {
        Long id = 1L;

        TransitionRequest request = new TransitionRequest(
                List.of(id),
                "initiator",
                "comment"
        );

        when(documentRepository.findAllById(request.getDocumentIds()))
                .thenReturn(List.of()); // пусто

        List<TransitionResult> results = documentService.submitDocuments(request);

        assertEquals(1, results.size());
        TransitionResult r = results.get(0);

        assertEquals(id, r.getDocumentId());
        assertEquals(TransitionStatus.NOT_FOUND, r.getStatus());

        verifyNoInteractions(documentHistoryRepository);
    }

    @Test
    void submitDocuments_conflict_submitted() {
        Long id = 1L;

        Document doc = new Document();
        doc.setId(id);
        doc.setStatus(DocumentStatus.SUBMITTED);

        TransitionRequest request = new TransitionRequest(
                List.of(id),
                "initiator",
                "comment"
        );

        when(documentRepository.findAllById(request.getDocumentIds()))
                .thenReturn(List.of(doc));

        List<TransitionResult> results = documentService.submitDocuments(request);

        assertEquals(1, results.size());
        TransitionResult r = results.get(0);

        assertEquals(TransitionStatus.CONFLICT, r.getStatus());
        verifyNoInteractions(documentHistoryRepository);
    }

    @Test
    void submitDocuments_conflict_approved() {
        Long id = 1L;

        Document doc = new Document();
        doc.setId(id);
        doc.setStatus(DocumentStatus.APPROVED);

        TransitionRequest request = new TransitionRequest(
                List.of(id),
                "initiator",
                "comment"
        );

        when(documentRepository.findAllById(request.getDocumentIds()))
                .thenReturn(List.of(doc));

        List<TransitionResult> results = documentService.submitDocuments(request);

        assertEquals(1, results.size());
        assertEquals(TransitionStatus.CONFLICT, results.get(0).getStatus());

        verifyNoInteractions(documentHistoryRepository);
    }

    @Test
    void submitDocuments_multipleDocuments() {
        Long id1 = 1L;
        Long id2 = 2L;

        Document doc1 = new Document();
        doc1.setId(id1);
        doc1.setStatus(DocumentStatus.DRAFT);

        Document doc2 = new Document();
        doc2.setId(id2);
        doc2.setStatus(DocumentStatus.SUBMITTED); // конфликт

        TransitionRequest request = new TransitionRequest(
                List.of(id1, id2),
                "initiator",
                "comment"
        );

        when(documentRepository.findAllById(request.getDocumentIds()))
                .thenReturn(List.of(doc1, doc2));

        List<TransitionResult> results = documentService.submitDocuments(request);

        assertEquals(2, results.size());

        assertEquals(TransitionStatus.SUCCESS, results.get(0).getStatus());
        assertEquals(TransitionStatus.CONFLICT, results.get(1).getStatus());

        verify(documentHistoryRepository, times(1)).save(any(DocumentHistory.class));
    }

    // ---------------------------------------------------------
    // approveDocuments
    // ---------------------------------------------------------

    @Test
    void approveDocuments_success() {
        Long id = 1L;

        Document doc = new Document();
        doc.setId(id);

        TransitionRequest request = new TransitionRequest(
                List.of(id),
                "initiator",
                "comment"
        );

        TransitionResult successResult = new TransitionResult();
        successResult.setDocumentId(id);
        successResult.setStatus(TransitionStatus.SUCCESS);

        when(documentRepository.findAllById(request.getDocumentIds()))
                .thenReturn(List.of(doc));

        when(documentApprovingService.approveDocument(id, request.getInitiator(), request.getComment()))
                .thenReturn(successResult);

        List<TransitionResult> results = documentService.approveDocuments(request);

        assertEquals(1, results.size());
        assertEquals(TransitionStatus.SUCCESS, results.get(0).getStatus());
        assertEquals(id, results.get(0).getDocumentId());
    }

    @Test
    void approveDocuments_notFound() {
        Long id = 1L;

        TransitionRequest request = new TransitionRequest(
                List.of(id),
                "initiator",
                "comment"
        );

        when(documentRepository.findAllById(request.getDocumentIds()))
                .thenReturn(List.of()); // документ не найден

        TransitionResult notFound = new TransitionResult();
        notFound.setDocumentId(id);
        notFound.setStatus(TransitionStatus.NOT_FOUND);

        when(documentApprovingService.approveDocument(id, request.getInitiator(), request.getComment()))
                .thenReturn(notFound);

        List<TransitionResult> results = documentService.approveDocuments(request);

        assertEquals(1, results.size());
        assertEquals(TransitionStatus.NOT_FOUND, results.get(0).getStatus());
    }

    @Test
    void approveDocuments_registryError() {
        Long id = 1L;

        Document doc = new Document();
        doc.setId(id);

        TransitionRequest request = new TransitionRequest(
                List.of(id),
                "initiator",
                "comment"
        );

        when(documentRepository.findAllById(request.getDocumentIds()))
                .thenReturn(List.of(doc));

        when(documentApprovingService.approveDocument(id, request.getInitiator(), request.getComment()))
                .thenThrow(new RuntimeException("DB error"));

        List<TransitionResult> results = documentService.approveDocuments(request);

        assertEquals(1, results.size());
        assertEquals(TransitionStatus.REGISTRY_ERROR, results.get(0).getStatus());
        assertEquals(id, results.get(0).getDocumentId());
    }

    @Test
    void approveDocuments_multiple() {
        Long id1 = 1L;
        Long id2 = 2L;

        Document doc1 = new Document();
        doc1.setId(id1);

        Document doc2 = new Document();
        doc2.setId(id2);

        TransitionRequest request = new TransitionRequest(
                List.of(id1, id2),
                "initiator",
                "comment"
        );

        when(documentRepository.findAllById(request.getDocumentIds()))
                .thenReturn(List.of(doc1, doc2));

        TransitionResult r1 = new TransitionResult();
        r1.setDocumentId(id1);
        r1.setStatus(TransitionStatus.SUCCESS);

        TransitionResult r2 = new TransitionResult();
        r2.setDocumentId(id2);
        r2.setStatus(TransitionStatus.CONFLICT);

        when(documentApprovingService.approveDocument(id1, request.getInitiator(), request.getComment()))
                .thenReturn(r1);

        when(documentApprovingService.approveDocument(id2, request.getInitiator(), request.getComment()))
                .thenReturn(r2);

        List<TransitionResult> results = documentService.approveDocuments(request);

        assertEquals(2, results.size());
        assertEquals(TransitionStatus.SUCCESS, results.get(0).getStatus());
        assertEquals(TransitionStatus.CONFLICT, results.get(1).getStatus());
    }

    @Test
    void approveDocuments_preservesOrder() {
        Long id1 = 10L;
        Long id2 = 5L;
        Long id3 = 7L;

        TransitionRequest request = new TransitionRequest(
                List.of(id1, id2, id3),
                "initiator",
                "comment"
        );

        when(documentRepository.findAllById(request.getDocumentIds()))
                .thenReturn(List.of()); // не важно

        TransitionResult r1 = new TransitionResult();
        r1.setDocumentId(id1);
        r1.setStatus(TransitionStatus.NOT_FOUND);

        TransitionResult r2 = new TransitionResult();
        r2.setDocumentId(id2);
        r2.setStatus(TransitionStatus.NOT_FOUND);

        TransitionResult r3 = new TransitionResult();
        r3.setDocumentId(id3);
        r3.setStatus(TransitionStatus.NOT_FOUND);

        when(documentApprovingService.approveDocument(anyLong(), anyString(), anyString()))
                .thenReturn(r1, r2, r3);

        List<TransitionResult> results = documentService.approveDocuments(request);

        assertEquals(id1, results.get(0).getDocumentId());
        assertEquals(id2, results.get(1).getDocumentId());
        assertEquals(id3, results.get(2).getDocumentId());
    }

    // ---------------------------------------------------------
    // search
    // ---------------------------------------------------------

    @Test
    void search_basicSuccess() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt"));
        Instant from = Instant.now().minusSeconds(1000);
        Instant to = Instant.now();

        Document doc = new Document();
        doc.setId(1L);

        DocumentResponse mapped = new DocumentResponse();
        mapped.setId(1L);

        Page<Document> page = new PageImpl<>(List.of(doc));

        when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        when(documentMapper.toDocumentResponse(doc)).thenReturn(mapped);

        Page<DocumentResponse> result = documentService.search(
                "Ivanov",
                "SUBMITTED",
                from,
                to,
                pageable
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));

        verify(documentRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void search_filtersApplied() {
        Pageable pageable = PageRequest.of(0, 10);

        when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        documentService.search("Ivanov", "SUBMITTED", Instant.now(), Instant.now(), pageable);

        ArgumentCaptor<Specification<Document>> specCaptor = ArgumentCaptor.forClass(Specification.class);

        verify(documentRepository).findAll(specCaptor.capture(), any(Pageable.class));

        Specification<Document> spec = specCaptor.getValue();

        assertNotNull(spec);
    }
}
