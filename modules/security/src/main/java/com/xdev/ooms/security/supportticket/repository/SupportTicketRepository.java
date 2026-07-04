package com.xdev.ooms.security.supportticket.repository;

import com.xdev.ooms.security.supportticket.entity.SupportTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {

    Page<SupportTicket> findByIsDeletedFalseOrderByCreatedDateDesc(Pageable pageable);

    Page<SupportTicket> findByTenantIdAndIsDeletedFalseOrderByCreatedDateDesc(UUID tenantId, Pageable pageable);

    Page<SupportTicket> findByReporterUserIdAndIsDeletedFalseOrderByCreatedDateDesc(UUID reporterUserId, Pageable pageable);

    Optional<SupportTicket> findByIdAndIsDeletedFalse(UUID id);
}
