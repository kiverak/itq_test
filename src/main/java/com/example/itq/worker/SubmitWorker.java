package com.example.itq.worker;

import com.example.itq.config.ProcessingProperties;
import com.example.itq.dto.TransitionRequest;
import com.example.itq.dto.TransitionResult;
import com.example.itq.dto.TransitionStatus;
import com.example.itq.model.Document;
import com.example.itq.model.DocumentStatus;
import com.example.itq.repository.DocumentRepository;
import com.example.itq.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class SubmitWorker {

    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final ProcessingProperties props;

    @Async("workerExecutor")
    @Scheduled(fixedDelayString = "${processing.submit-interval-ms}")
    public void submitDraftDocuments() {

        try {
            List<Document> batch = documentRepository.findTopNByStatus(DocumentStatus.DRAFT, props.getBatchSize());

            if (batch.isEmpty()) {
                return;
            }

            List<Long> ids = batch.stream()
                    .map(Document::getId)
                    .toList();

            TransitionRequest request = new TransitionRequest(ids, "system-submit", "auto-submit");

            List<TransitionResult> results = documentService.submitDocuments(request);

            long ok = results.stream().filter(r -> r.getStatus() == TransitionStatus.SUCCESS).count();
            long fail = results.size() - ok;

            log.info("SUBMIT-worker: processed={}, success={}, failed={}", results.size(), ok, fail);

        } catch (Exception e) {
            log.error("SUBMIT-worker error: {}", e.getMessage(), e);
        }
    }
}
