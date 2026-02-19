package com.example.itq.service;

import com.example.itq.dto.TransitionResult;
import com.example.itq.dto.TransitionStatus;
import com.example.itq.model.*;
import com.example.itq.repository.ApprovalRegistryRepository;
import com.example.itq.repository.DocumentHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentApprovingServiceImpl implements DocumentApprovingService {

    private final DocumentHistoryRepository documentHistoryRepository;
    private final ApprovalRegistryRepository approvalRegistryRepository;

    @Override
    @Transactional
    public TransitionResult approveDocument(Long id, Document document, String initiator, String comment) throws Exception {
        TransitionResult result = new TransitionResult();
        result.setDocumentId(id);

        if (document == null) {
            result.setStatus(TransitionStatus.NOT_FOUND);
            return result;
        } else if (document.getStatus() != DocumentStatus.SUBMITTED) {
            result.setStatus(TransitionStatus.CONFLICT);
        } else {
            document.setStatus(DocumentStatus.APPROVED);
            result.setStatus(TransitionStatus.SUCCESS);

            DocumentHistory history = new DocumentHistory();
            history.setDocument(document);
            history.setAction(DocumentAction.APPROVE);
            history.setInitiator(initiator);
            history.setComment(comment);
            documentHistoryRepository.save(history);

            ApprovalRegistry approvalRegistry = new ApprovalRegistry();
            approvalRegistry.setDocument(document);
            approvalRegistryRepository.save(approvalRegistry);
        }

        return result;
    }
}
