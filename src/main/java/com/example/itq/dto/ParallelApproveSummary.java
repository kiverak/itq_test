package com.example.itq.dto;

import com.example.itq.model.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParallelApproveSummary {
    private int success;
    private int conflict;
    private int registryError;
    private DocumentStatus finalStatus;
}
