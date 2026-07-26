package com.xdev.ooms.hr.document.controller;

import com.xdev.ooms.hr.document.dto.EmployeeDocumentDto;
import com.xdev.ooms.hr.document.entity.EmployeeDocument;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/employee-documents")
public class EmployeeDocumentController extends BaseControllerImpl<EmployeeDocument, EmployeeDocumentDto, EmployeeDocumentDto> {

    public EmployeeDocumentController(
            BaseService<EmployeeDocument, EmployeeDocumentDto, EmployeeDocumentDto> baseService,
            ModelMapper modelMapper
    ) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "EMPLOYEEDOCUMENT";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
