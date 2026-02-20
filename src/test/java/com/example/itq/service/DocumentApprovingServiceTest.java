package com.example.itq.service;

import com.example.itq.dto.TransitionResult;
import com.example.itq.dto.TransitionStatus;
import com.example.itq.model.*;
import com.example.itq.repository.ApprovalRegistryRepository;
import com.example.itq.repository.DocumentHistoryRepository;
import com.example.itq.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentApprovingServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentHistoryRepository documentHistoryRepository;

    @Mock
    private ApprovalRegistryRepository approvalRegistryRepository;

    @InjectMocks
    private DocumentApprovingServiceImpl documentApprovingService;

    // ---------------------------------------------------------
    // approveDocument
    // ---------------------------------------------------------

    @Test
    void approveDocument_notFound() {
        Long id = 1L;

        when(documentRepository.findById(id)).thenReturn(Optional.empty());

        TransitionResult result = documentApprovingService.approveDocument(id, "init", "comment");

        assertEquals(id, result.getDocumentId());
        assertEquals(TransitionStatus.NOT_FOUND, result.getStatus());

        verifyNoInteractions(documentHistoryRepository);
        verifyNoInteractions(approvalRegistryRepository);
    }

    @Test
    void approveDocument_conflict_wrongStatus() {
        Long id = 1L;

        Document doc = new Document();
        doc.setId(id);
        doc.setStatus(DocumentStatus.DRAFT);

        when(documentRepository.findById(id)).thenReturn(Optional.of(doc));

        TransitionResult result = documentApprovingService.approveDocument(id, "init", "comment");

        assertEquals(id, result.getDocumentId());
        assertEquals(TransitionStatus.CONFLICT, result.getStatus());

        verifyNoInteractions(documentHistoryRepository);
        verifyNoInteractions(approvalRegistryRepository);
    }

    @Test
    void approveDocument_success() {
        Long id = 1L;

        Document doc = new Document();
        doc.setId(id);
        doc.setStatus(DocumentStatus.SUBMITTED);

        when(documentRepository.findById(id)).thenReturn(Optional.of(doc));

        TransitionResult result = documentApprovingService.approveDocument(id, "init", "comment");

        assertEquals(id, result.getDocumentId());
        assertEquals(TransitionStatus.SUCCESS, result.getStatus());
        assertEquals(DocumentStatus.APPROVED, doc.getStatus());

        verify(documentHistoryRepository, times(1)).save(any(DocumentHistory.class));
        verify(approvalRegistryRepository, times(1)).save(any(ApprovalRegistry.class));
    }

    @Test
    void approveDocument_historySaved() {
        Long id = 1L;

        Document doc = new Document();
        doc.setId(id);
        doc.setStatus(DocumentStatus.SUBMITTED);

        when(documentRepository.findById(id)).thenReturn(Optional.of(doc));

        documentApprovingService.approveDocument(id, "initiator", "comment");

        ArgumentCaptor<DocumentHistory> captor = ArgumentCaptor.forClass(DocumentHistory.class);
        verify(documentHistoryRepository).save(captor.capture());

        DocumentHistory saved = captor.getValue();

        assertEquals(doc, saved.getDocument());
        assertEquals(DocumentAction.APPROVE, saved.getAction());
        assertEquals("initiator", saved.getInitiator());
        assertEquals("comment", saved.getComment());
    }

    @Test
    void approveDocument_registrySaved() {
        Long id = 1L;

        Document doc = new Document();
        doc.setId(id);
        doc.setStatus(DocumentStatus.SUBMITTED);

        when(documentRepository.findById(id)).thenReturn(Optional.of(doc));

        documentApprovingService.approveDocument(id, "init", "comment");

        ArgumentCaptor<ApprovalRegistry> captor = ArgumentCaptor.forClass(ApprovalRegistry.class);
        verify(approvalRegistryRepository).save(captor.capture());

        ApprovalRegistry saved = captor.getValue();

        assertEquals(doc, saved.getDocument());
    }
}
