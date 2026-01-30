package com.medibook.api.controller;

import com.medibook.api.dto.Family.FamilyMemberCreateRequestDTO;
import com.medibook.api.dto.Family.FamilyMemberDTO;
import com.medibook.api.entity.User;
import com.medibook.api.service.FamilyService;
import com.medibook.api.util.AuthorizationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    @PostMapping
    public ResponseEntity<Object> registerFamilyMember(
            @Valid @RequestBody FamilyMemberCreateRequestDTO dto,
            HttpServletRequest request) {

        User authenticatedUser = (User) request.getAttribute("authenticatedUser");

        if (!AuthorizationUtil.isPatient(authenticatedUser)) {
            return new ResponseEntity<>(
                    Map.of("error", "Forbidden", "message", "Only patients can add family members"),
                    HttpStatus.FORBIDDEN);
        }

        try {
            FamilyMemberDTO createdMember = familyService.createFamilyMember(authenticatedUser.getId(), dto);
            return new ResponseEntity<>(createdMember, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    Map.of("error", "Bad Request", "message", e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<Object> getMyFamilyMembers(HttpServletRequest request) {
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");

        if (!AuthorizationUtil.isPatient(authenticatedUser)) {
            return new ResponseEntity<>(
                    Map.of("error", "Forbidden", "message", "Only patients can view family members"),
                    HttpStatus.FORBIDDEN);
        }

        List<FamilyMemberDTO> members = familyService.getFamilyMembersByHolder(authenticatedUser.getId());
        return ResponseEntity.ok(members);
    }
}