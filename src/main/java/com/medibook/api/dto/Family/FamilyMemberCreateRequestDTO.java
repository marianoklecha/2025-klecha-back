package com.medibook.api.dto.Family;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FamilyMemberCreateRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El apellido es obligatorio")
    private String surname;

    @NotNull(message = "El DNI es obligatorio")
    private Long dni;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDate birthdate;

    @NotBlank(message = "El género es obligatorio")
    private String gender;

    @NotBlank(message = "El tipo de relación es obligatorio")
    private String relationship;
}
