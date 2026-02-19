package com.example.itq.service;

import com.example.itq.dto.*;
import com.example.itq.model.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DocumentApprovingService {
    TransitionResult approveDocument(Long id, Document document, String initiator, String comment) throws Exception;
}
