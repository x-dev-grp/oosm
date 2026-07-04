package com.xdev.ooms.hr.payslip.controller;

import com.xdev.ooms.hr.common.HrPermissionSupport;
import com.xdev.ooms.hr.payslip.dto.PayslipDto;
import com.xdev.ooms.hr.payslip.entity.Payslip;
import com.xdev.ooms.hr.payslip.service.PayslipPdfService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/hr/payslips")
public class PayslipController extends BaseControllerImpl<Payslip, PayslipDto, PayslipDto> {

    private final PayslipPdfService payslipPdfService;

    public PayslipController(
            BaseService<Payslip, PayslipDto, PayslipDto> baseService,
            ModelMapper modelMapper,
            PayslipPdfService payslipPdfService
    ) {
        super(baseService, modelMapper);
        this.payslipPdfService = payslipPdfService;
    }

    @Override
    protected String getResourceName() {
        return "PAYSLIP";
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable UUID id) {
        HrPermissionSupport.requireAction("PAYSLIP", Action.GEN_PDF);
        byte[] content = payslipPdfService.generatePayslipPdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"payslip-" + id + ".pdf\"");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(content);
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
