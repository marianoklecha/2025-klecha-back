package com.medibook.api.dto.Turn;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class TurnCreateRequestDTO {
    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;
    
    @NotNull(message = "Patient ID is required") 
    private UUID patientId;
    
    @NotNull(message = "Scheduled time is required")
    @Future(message = "The appointment date must be in the future")
    private OffsetDateTime scheduledAt;
    
    @Size(max = 500, message = "Motive cannot exceed 500 characters")
    private String motive;

    private UUID familyMemberId;
}
