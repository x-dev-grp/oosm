package com.xdev.ooms.security.internal;

import com.xdev.ooms.security.supportticket.dto.CreateSupportTicketRequest;
import com.xdev.ooms.security.supportticket.dto.SupportTicketDto;
import com.xdev.ooms.security.supportticket.service.SupportTicketService;
import com.xdev.ooms.sharedkernel.ports.SupportTicketCreateCommand;
import com.xdev.ooms.sharedkernel.ports.SupportTicketPort;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupportTicketPortImpl implements SupportTicketPort {

    private final SupportTicketService supportTicketService;

    public SupportTicketPortImpl(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String create(SupportTicketCreateCommand command) {
        if (command == null) {
            return null;
        }
        try {
            CreateSupportTicketRequest request = new CreateSupportTicketRequest();
            request.setSubject(command.getSubject());
            request.setDescription(command.getDescription());
            request.setPriority(command.getPriority());
            request.setPageUrl(command.getPageUrl());
            SupportTicketDto saved = supportTicketService.create(request);
            String id = saved != null && saved.getId() != null ? saved.getId().toString() : null;
            OOSMLogger.logBusinessEvent(getClass(), "SUPPORT_TICKET_CREATED",
                    "id=" + id + " subject=" + command.getSubject());
            return id;
        } catch (Exception e) {
            OOSMLogger.logException(getClass(), "Failed to create support ticket", e);
            return null;
        }
    }
}
