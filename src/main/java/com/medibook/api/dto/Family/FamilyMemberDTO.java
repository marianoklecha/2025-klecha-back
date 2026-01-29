package com.medibook.api.dto.Family;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class FamilyMemberDTO {
    private UUID id;
    private UUID holderId;
    private String name;
    private String surname;
    private Long dni;
    private LocalDate birthdate;
    private String gender;
}