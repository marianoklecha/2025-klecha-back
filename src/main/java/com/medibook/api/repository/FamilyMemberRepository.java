package com.medibook.api.repository;

import com.medibook.api.entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, UUID> {

    boolean existsByDni(Long dni);
    
    List<FamilyMember> findByHolderId(UUID holderId);

    List<FamilyMember> findByHolderIdIn(List<UUID> holderIds);
}