package com.medibook.api.dto.Family;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FamilyMemberCreateRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$", message = "El nombre solo puede tener letras y espacios")
    String name;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$", message = "El apellido solo puede tener letras y espacios")
    String surname;

    @NotNull(message = "El DNI es obligatorio")
    @Min(value = 1000000L, message = "El DNI debe tener al menos 7 dígitos")
    @Max(value = 99999999L, message = "El DNI debe tener máximo 8 dígitos")
    Long dni;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDate birthdate;

    @NotBlank(message = "El género es obligatorio")
    @Pattern(regexp = "^(MALE|FEMALE)$", message = "El género debe ser MALE o FEMALE")
    String gender;

    @NotBlank(message = "El tipo de relación es obligatorio")
    @Pattern(regexp = "^(Hijo|Hija|Madre|Padre|Hermano|Hermana)$", message = "La relación no es válida")
    private String relationship;
}
