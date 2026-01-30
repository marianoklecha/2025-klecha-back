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
}
