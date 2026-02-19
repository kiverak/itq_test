package com.example.itq.dto;

import lombok.Data;
import java.util.List;

@Data
public class TransitionRequest {
    private List<Long> documentIds;
    private String initiator;
    private String comment;
}
