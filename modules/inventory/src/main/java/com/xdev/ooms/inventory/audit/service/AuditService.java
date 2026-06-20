package com.xdev.ooms.inventory.audit.service;

import com.xdev.ooms.sharedkernel.dtos.AuditDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class AuditService {

    @Transactional(readOnly = true)
    public List<AuditDto> getAllAudits() {
        return Collections.emptyList();
    }
}
