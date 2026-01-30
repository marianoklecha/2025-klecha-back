package com.medibook.api.service;

import com.medibook.api.dto.Turn.TurnCreateRequestDTO;
import com.medibook.api.dto.Turn.TurnResponseDTO;
import com.medibook.api.entity.FamilyMember;
import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.User;
import com.medibook.api.mapper.TurnAssignedMapper;
import com.medibook.api.repository.FamilyMemberRepository;
import com.medibook.api.repository.TurnAssignedRepository;
import com.medibook.api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FamilyTurnServiceTest {

    @Mock
    private TurnAssignedRepository turnRepo;
    @Mock
    private UserRepository userRepo;
    @Mock
    private FamilyMemberRepository familyMemberRepository;
    @Mock
    private TurnAssignedMapper mapper;
    
    @Mock private NotificationService notificationService;
    @Mock private EmailService emailService;
    @Mock private BadgeEvaluationTriggerService badgeEvaluationTrigger;

    @InjectMocks
    private TurnAssignedService turnService;

    private User doctor;
    private User patient;
    private FamilyMember familyMember;
    private TurnCreateRequestDTO turnRequest;
    private UUID doctorId;
    private UUID patientId;
    private UUID familyId;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        familyId = UUID.randomUUID();

        doctor = new User();
        doctor.setId(doctorId);
        doctor.setRole("DOCTOR");
        doctor.setStatus("ACTIVE");
        doctor.setName("Dr. House");
        doctor.setEmail("house@medibook.com");

        patient = new User();
        patient.setId(patientId);
        patient.setRole("PATIENT");
        patient.setStatus("ACTIVE");
        patient.setName("Patient Zero");
        patient.setEmail("patient@medibook.com");

        familyMember = new FamilyMember();
        familyMember.setId(familyId);
        familyMember.setHolder(patient); 
        familyMember.setName("Hijo");

        turnRequest = new TurnCreateRequestDTO();
        turnRequest.setDoctorId(doctorId);
        turnRequest.setPatientId(patientId);
        turnRequest.setScheduledAt(OffsetDateTime.now().plusDays(1));
    }

    @Test
    void createTurn_ForPatient_Success() {
        // Arrange
        when(userRepo.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepo.findById(patientId)).thenReturn(Optional.of(patient));
        when(turnRepo.existsByDoctor_IdAndScheduledAtAndStatusNotCancelled(any(), any())).thenReturn(false);
        
        TurnAssigned savedTurn = new TurnAssigned();
        savedTurn.setId(UUID.randomUUID());
        savedTurn.setScheduledAt(turnRequest.getScheduledAt());
        savedTurn.setDoctor(doctor);
        
        when(turnRepo.save(any(TurnAssigned.class))).thenReturn(savedTurn);
        TurnResponseDTO responseDTO = TurnResponseDTO.builder()
                .id(savedTurn.getId())
                .build();
        when(mapper.toDTO(any(TurnAssigned.class))).thenReturn(responseDTO);

        // Act
        TurnResponseDTO result = turnService.createTurn(turnRequest);

        // Assert
        assertNotNull(result);
        verify(turnRepo).save(argThat(turn -> 
            turn.getPatient().equals(patient) && 
            turn.getFamilyMember() == null
        ));
    }

    @Test
    void createTurn_ForFamilyMember_Success() {
        // Arrange
        turnRequest.setFamilyMemberId(familyId); 

        when(userRepo.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepo.findById(patientId)).thenReturn(Optional.of(patient));
        when(familyMemberRepository.findById(familyId)).thenReturn(Optional.of(familyMember));
        when(turnRepo.existsByDoctor_IdAndScheduledAtAndStatusNotCancelled(any(), any())).thenReturn(false);

        TurnAssigned savedTurn = new TurnAssigned();
        savedTurn.setId(UUID.randomUUID());
        savedTurn.setScheduledAt(turnRequest.getScheduledAt());
        savedTurn.setDoctor(doctor);

        when(turnRepo.save(any(TurnAssigned.class))).thenReturn(savedTurn);
        TurnResponseDTO responseDTO = TurnResponseDTO.builder()
                .id(savedTurn.getId())
                .build();
        when(mapper.toDTO(any(TurnAssigned.class))).thenReturn(responseDTO);

        // Act
        TurnResponseDTO result = turnService.createTurn(turnRequest);

        // Assert
        assertNotNull(result);
        verify(turnRepo).save(argThat(turn -> 
            turn.getPatient().equals(patient) && 
            turn.getFamilyMember().equals(familyMember)
        ));
    }

    @Test
    void createTurn_FamilyMemberNotBelongToPatient_ThrowsException() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        
        FamilyMember strangerMember = new FamilyMember();
        strangerMember.setId(UUID.randomUUID());
        strangerMember.setHolder(otherUser);

        turnRequest.setFamilyMemberId(strangerMember.getId());

        when(userRepo.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepo.findById(patientId)).thenReturn(Optional.of(patient));
        when(familyMemberRepository.findById(strangerMember.getId())).thenReturn(Optional.of(strangerMember));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            turnService.createTurn(turnRequest);
        });

        assertEquals("El familiar indicado no pertenece al paciente", exception.getMessage());
        verify(turnRepo, never()).save(any());
    }

    @Test
    @DisplayName("Debe fallar si el ID del familiar no existe")
    void createTurn_FamilyMemberNotFound_ThrowsException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        turnRequest.setFamilyMemberId(nonExistentId);

        when(userRepo.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepo.findById(patientId)).thenReturn(Optional.of(patient));
        when(familyMemberRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            turnService.createTurn(turnRequest);
        });
        
        verify(turnRepo, never()).save(any());
    }
}