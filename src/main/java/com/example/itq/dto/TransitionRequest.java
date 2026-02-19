package com.example.itq.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class TransitionRequest {

    @Size(max = 1000, message = "documentIds не может содержать более 1000 элементов")
    private List<Long> documentIds;

    private String initiator;
    private String comment;
}
