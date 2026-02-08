package com.medibook.api.service;

import com.medibook.api.dto.Family.FamilyMemberCreateRequestDTO;
import com.medibook.api.dto.Family.FamilyMemberDTO;
import com.medibook.api.entity.FamilyMember;
import com.medibook.api.entity.User;
import com.medibook.api.repository.FamilyMemberRepository;
import com.medibook.api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private final FamilyMemberRepository familyMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public FamilyMemberDTO createFamilyMember(UUID holderId, FamilyMemberCreateRequestDTO dto) {
        User holder = userRepository.findById(holderId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario titular no encontrado"));

        if (userRepository.existsByDni(dto.getDni())) {
             throw new IllegalArgumentException("El DNI ya pertenece a un usuario registrado");
        }
        if (familyMemberRepository.existsByDni(dto.getDni())) {
             throw new IllegalArgumentException("El DNI ya pertenece a un familiar registrado");
        }

        FamilyMember familyMember = new FamilyMember();
        familyMember.setHolder(holder);
        familyMember.setName(dto.getName());
        familyMember.setSurname(dto.getSurname());
        familyMember.setDni(dto.getDni());
        familyMember.setBirthdate(dto.getBirthdate());
        familyMember.setGender(dto.getGender());
        familyMember.setRelationship(dto.getRelationship());

        FamilyMember savedMember = familyMemberRepository.save(familyMember);

        return mapToDTO(savedMember);
    }

    @Transactional
    public FamilyMemberDTO updateFamilyMember(UUID holderId, UUID familyMemberId, FamilyMemberCreateRequestDTO dto) {
        FamilyMember familyMember = familyMemberRepository.findById(familyMemberId)
            .orElseThrow(() -> new EntityNotFoundException("Familiar no encontrado"));
        
            if (!familyMember.getHolder().getId().equals(holderId)) {
                throw new EntityNotFoundException("El id proporcionado no pertenece al grupo familiar");
            }

            if (!familyMember.getDni().equals(dto.getDni()) &&
                (userRepository.existsByDni(dto.getDni()) || familyMemberRepository.existsByDni(dto.getDni()))) {
                    throw new IllegalArgumentException("El DNI proporcionado ya se encuentra en uso");
            }

            familyMember.setName(dto.getName());
            familyMember.setSurname(dto.getSurname());
            familyMember.setDni(dto.getDni());
            familyMember.setBirthdate(dto.getBirthdate());
            familyMember.setGender(dto.getGender());
            familyMember.setRelationship(dto.getRelationship());

            FamilyMember updatedFamilyMember = familyMemberRepository.save(familyMember);
            return mapToDTO(updatedFamilyMember);
    }

    @Transactional(readOnly = true)
    public List<FamilyMemberDTO> getFamilyMembersByHolder(UUID holderId) {
        return familyMemberRepository.findByHolderId(holderId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private FamilyMemberDTO mapToDTO(FamilyMember entity) {
        return FamilyMemberDTO.builder()
                .id(entity.getId())
                .holderId(entity.getHolder().getId())
                .name(entity.getName())
                .surname(entity.getSurname())
                .dni(entity.getDni())
                .birthdate(entity.getBirthdate())
                .gender(entity.getGender())
                .relationship(entity.getRelationship())
                .build();
    }

    @Transactional(readOnly = true)
    public Map<UUID, List<FamilyMemberDTO>> getFamilyMembersByHolder(List<UUID> holderIds) {
        if (holderIds == null || holderIds.isEmpty()) {
            return new java.util.HashMap<>();
        }

        List<FamilyMember> allMembers = familyMemberRepository.findByHolderIdIn(holderIds);

        return allMembers.stream()
            .map(this::mapToDTO)
            .collect(Collectors.groupingBy(FamilyMemberDTO::getHolderId));
    }
}