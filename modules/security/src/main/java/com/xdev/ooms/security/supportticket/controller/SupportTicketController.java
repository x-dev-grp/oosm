package com.xdev.ooms.security.supportticket.controller;

import com.xdev.ooms.security.supportticket.dto.CreateSupportTicketRequest;
import com.xdev.ooms.security.supportticket.dto.SupportTicketDto;
import com.xdev.ooms.security.supportticket.dto.SupportTicketPageResponse;
import com.xdev.ooms.security.supportticket.dto.UpdateSupportTicketRequest;
import com.xdev.ooms.security.supportticket.entity.SupportTicket;
import com.xdev.ooms.security.supportticket.service.SupportTicketService;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiSingleResponse;
import com.xdev.ooms.sharedkernel.utils.ExceptionHandler;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/security/support-tickets")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    public SupportTicketController(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    @PostMapping
    public ResponseEntity<ApiSingleResponse<SupportTicket, SupportTicketDto>> create(
            @RequestBody CreateSupportTicketRequest request) {
        try {
            SupportTicketDto dto = supportTicketService.create(request);
            return ResponseEntity.ok(new ApiSingleResponse<>(true, "Support ticket created", dto));
        } catch (Exception ex) {
            return ExceptionHandler.handleSingleException(this.getClass(), "create", ex);
        }
    }

    @GetMapping
    public ResponseEntity<SupportTicketPageResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "mine") String scope) {
        try {
            Page<SupportTicketDto> tickets = supportTicketService.listForCurrentUser(page, size, scope);
            return ResponseEntity.ok(new SupportTicketPageResponse(
                    true,
                    "Support tickets loaded",
                    tickets.getContent(),
                    tickets.getTotalElements(),
                    tickets.getNumber() + 1,
                    tickets.getTotalPages()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(new SupportTicketPageResponse(
                    false,
                    ex.getMessage(),
                    null,
                    0,
                    page + 1,
                    0));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiSingleResponse<SupportTicket, SupportTicketDto>> getById(@PathVariable UUID id) {
        try {
            SupportTicketDto dto = supportTicketService.getById(id);
            return ResponseEntity.ok(new ApiSingleResponse<>(true, "Support ticket loaded", dto));
        } catch (Exception ex) {
            return ExceptionHandler.handleSingleException(this.getClass(), "getById", ex);
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiSingleResponse<SupportTicket, SupportTicketDto>> update(
            @PathVariable UUID id,
            @RequestBody UpdateSupportTicketRequest request) {
        try {
            SupportTicketDto dto = supportTicketService.update(id, request);
            return ResponseEntity.ok(new ApiSingleResponse<>(true, "Support ticket updated", dto));
        } catch (Exception ex) {
            return ExceptionHandler.handleSingleException(this.getClass(), "update", ex);
        }
    }
}
