package com.medibook.api.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.medibook.api.dto.Family.FamilyMemberCreateRequestDTO;
import com.medibook.api.dto.Family.FamilyMemberDTO;
import com.medibook.api.entity.FamilyMember;
import com.medibook.api.entity.User;
import com.medibook.api.repository.FamilyMemberRepository;
import com.medibook.api.repository.UserRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FamilyServiceTest {

    @Mock
    private FamilyMemberRepository familyMemberRepository;
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private FamilyService familyService;

    @Test
    void createFamilyMember_Success() {
        //Arrange
        UUID holderId = UUID.randomUUID();
        User holder = new User();
        holder.setId(holderId);

        FamilyMemberCreateRequestDTO requestDTO = new FamilyMemberCreateRequestDTO();
        requestDTO.setName("Hijo");
        requestDTO.setSurname("Test");
        requestDTO.setDni(44444444L);
        requestDTO.setBirthdate(LocalDate.of(2010,1,1));
        requestDTO.setGender("Masculino");
        requestDTO.setRelationship("Hijo");

        FamilyMember savedMember = new FamilyMember();
        savedMember.setId(UUID.randomUUID());
        savedMember.setHolder(holder);
        savedMember.setName("Hijo");
        savedMember.setSurname("Test");
        savedMember.setDni(12345678L);
        savedMember.setBirthdate(LocalDate.of(2010, 1, 1));
        savedMember.setGender("M");
        savedMember.setRelationship("Hijo");

        when(userRepository.findById(holderId)).thenReturn(Optional.of(holder));
        when(userRepository.existsByDni(requestDTO.getDni())).thenReturn(false);
        when(familyMemberRepository.existsByDni(requestDTO.getDni())).thenReturn(false);
        when(familyMemberRepository.save(any(FamilyMember.class))).thenReturn(savedMember);

        //Act
        FamilyMemberDTO result = familyService.createFamilyMember(holderId, requestDTO);

        //Assert
        assertNotNull(result);
        assertEquals("Hijo", result.getName());
        assertEquals("Hijo", result.getRelationship());
        assertEquals(holderId, result.getHolderId());
        
        verify(familyMemberRepository).save(any(FamilyMember.class));
    }

    @Test
    void createFamilyMember_HolderNotFound_ThrowsException() {
        // Arrange
        UUID holderId = UUID.randomUUID();
        FamilyMemberCreateRequestDTO requestDTO = new FamilyMemberCreateRequestDTO();

        when(userRepository.findById(holderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> 
            familyService.createFamilyMember(holderId, requestDTO)
        );
        
        verify(familyMemberRepository, never()).save(any());
    }

    @Test
    void createFamilyMember_DniExistsInUsers_ThrowsException() {
        // Arrange
        UUID holderId = UUID.randomUUID();
        User holder = new User();
        
        FamilyMemberCreateRequestDTO requestDTO = new FamilyMemberCreateRequestDTO();
        requestDTO.setDni(12345678L);

        when(userRepository.findById(holderId)).thenReturn(Optional.of(holder));
        when(userRepository.existsByDni(requestDTO.getDni())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            familyService.createFamilyMember(holderId, requestDTO)
        );

        verify(familyMemberRepository, never()).save(any());
    }

    @Test
    void createFamilyMember_DniExistsInFamily_ThrowsException() {
        // Arrange
        UUID holderId = UUID.randomUUID();
        User holder = new User();
        
        FamilyMemberCreateRequestDTO requestDTO = new FamilyMemberCreateRequestDTO();
        requestDTO.setDni(12345678L);

        when(userRepository.findById(holderId)).thenReturn(Optional.of(holder));
        when(userRepository.existsByDni(requestDTO.getDni())).thenReturn(false);
        when(familyMemberRepository.existsByDni(requestDTO.getDni())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            familyService.createFamilyMember(holderId, requestDTO)
        );

        verify(familyMemberRepository, never()).save(any());
    }

    @Test
    void getFamilyMembersByHolder_ReturnsList() {
        // Arrange
        UUID holderId = UUID.randomUUID();
        User holder = new User();
        holder.setId(holderId);

        FamilyMember member = new FamilyMember();
        member.setId(UUID.randomUUID());
        member.setName("Familiar");
        member.setSurname("Uno");
        member.setHolder(holder);
        member.setDni(11111L);
        member.setBirthdate(LocalDate.of(2015, 5, 20));
        member.setGender("F");
        member.setRelationship("Hija");

        when(familyMemberRepository.findByHolderId(holderId)).thenReturn(List.of(member));

        // Act
        List<FamilyMemberDTO> result = familyService.getFamilyMembersByHolder(holderId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Familiar", result.get(0).getName());
        assertEquals("Hija", result.get(0).getRelationship());
    }

    @Test
    void getFamilyMembersByHolder_ReturnsEmptyList() {
        // Arrange
        UUID holderId = UUID.randomUUID();
        when(familyMemberRepository.findByHolderId(holderId)).thenReturn(Collections.emptyList());

        // Act
        List<FamilyMemberDTO> result = familyService.getFamilyMembersByHolder(holderId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateFamilyMember_Success() {
        // Arrange
        UUID holderId = UUID.randomUUID();
        User holder = new User();
        holder.setId(holderId);

        UUID memberId = UUID.randomUUID();
        FamilyMember existingMember = new FamilyMember();
        existingMember.setId(memberId);
        existingMember.setHolder(holder);
        existingMember.setName("OldName");
        existingMember.setSurname("OldSurname");
        existingMember.setDni(11111111L);
        existingMember.setBirthdate(LocalDate.of(2010, 1, 1));
        existingMember.setGender("M");
        existingMember.setRelationship("Hijo");

        FamilyMemberCreateRequestDTO updateDTO = new FamilyMemberCreateRequestDTO();
        updateDTO.setName("NewName");
        updateDTO.setSurname("NewSurname");
        updateDTO.setDni(11111111L); // Same DNI
        updateDTO.setBirthdate(LocalDate.of(2010, 1, 1));
        updateDTO.setGender("M");
        updateDTO.setRelationship("Hijo");

        when(familyMemberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));
        when(familyMemberRepository.save(any(FamilyMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        FamilyMemberDTO result = familyService.updateFamilyMember(holderId, memberId, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("NewName", result.getName());
        assertEquals("NewSurname", result.getSurname());
        verify(familyMemberRepository).save(existingMember);
    }

    @Test
    void updateFamilyMember_MemberNotFound_ThrowsException() {
        // Arrange
        UUID holderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        FamilyMemberCreateRequestDTO updateDTO = new FamilyMemberCreateRequestDTO();

        when(familyMemberRepository.findById(memberId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> 
            familyService.updateFamilyMember(holderId, memberId, updateDTO)
        );
        verify(familyMemberRepository, never()).save(any());
    }

    @Test
    void updateFamilyMember_UnauthorizedHolder_ThrowsException() {
        // Arrange
        UUID holderId = UUID.randomUUID();
        UUID otherHolderId = UUID.randomUUID();
        
        User otherHolder = new User();
        otherHolder.setId(otherHolderId);

        UUID memberId = UUID.randomUUID();
        FamilyMember existingMember = new FamilyMember();
        existingMember.setId(memberId);
        existingMember.setHolder(otherHolder); // Different holder

        FamilyMemberCreateRequestDTO updateDTO = new FamilyMemberCreateRequestDTO();

        when(familyMemberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> 
            familyService.updateFamilyMember(holderId, memberId, updateDTO)
        );
        verify(familyMemberRepository, never()).save(any());
    }

    @Test
    void updateFamilyMember_DniCollision_ThrowsException() {
        // Arrange
        UUID holderId = UUID.randomUUID();
        User holder = new User();
        holder.setId(holderId);

        UUID memberId = UUID.randomUUID();
        FamilyMember existingMember = new FamilyMember();
        existingMember.setId(memberId);
        existingMember.setHolder(holder);
        existingMember.setDni(11111111L);

        FamilyMemberCreateRequestDTO updateDTO = new FamilyMemberCreateRequestDTO();
        updateDTO.setDni(22222222L); // New DNI

        when(familyMemberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));
        when(userRepository.existsByDni(updateDTO.getDni())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            familyService.updateFamilyMember(holderId, memberId, updateDTO)
        );
        verify(familyMemberRepository, never()).save(any());
    }
}
