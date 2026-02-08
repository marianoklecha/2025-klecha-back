package com.medibook.api.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class DoctorPublicDTO {
    private UUID id;
    private String name;
    private String surname;
    private String medicalLicense;
    private String specialty;
    private Double score;
}
