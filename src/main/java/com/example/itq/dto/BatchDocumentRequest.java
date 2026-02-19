package com.example.itq.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchDocumentRequest {
    private List<Long> ids;
}
