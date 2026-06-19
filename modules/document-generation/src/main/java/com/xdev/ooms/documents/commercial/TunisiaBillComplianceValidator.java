package com.xdev.ooms.documents.commercial;

import com.xdev.ooms.documents.commercial.dto.BillGenerationRequest;
import com.xdev.ooms.documents.commercial.dto.BillLineDto;
import com.xdev.ooms.documents.commercial.dto.BillPartyDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class TunisiaBillComplianceValidator {

    public void validate(BillGenerationRequest request) {
        List<String> errors = new ArrayList<>();

        if (request == null) {
            throw new IllegalArgumentException("Bill payload is required");
        }
        requireText(request.getInvoiceNumber(), "Invoice number is required", errors);
        if (request.getOperationDate() == null) {
            errors.add("Operation date is required");
        }
        validateParty(request.getIssuer(), "Issuer", true, errors);
        validateParty(request.getClient(), "Client", false, errors);
        validateLines(request.getLines(), errors);

        if (request.isElectronicInvoice()) {
            requireText(request.getTtnReference(), "TTN unique reference is required for electronic invoices", errors);
            requireText(request.getIssuerElectronicSeal(), "Issuer electronic seal/signature is required for electronic invoices", errors);
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }
    }

    private void validateParty(BillPartyDto party, String label, boolean taxIdRequired, List<String> errors) {
        if (party == null) {
            errors.add(label + " is required");
            return;
        }
        requireText(party.getDisplayName(), label + " name/legal name is required", errors);
        requireText(party.getAddress(), label + " address is required", errors);
        if (taxIdRequired) {
            requireText(party.getTaxRegistrationNumber(), label + " tax registration number is required", errors);
        }
    }

    private void validateLines(List<BillLineDto> lines, List<String> errors) {
        if (lines == null || lines.isEmpty()) {
            errors.add("At least one bill line is required");
            return;
        }

        for (int i = 0; i < lines.size(); i++) {
            BillLineDto line = lines.get(i);
            String prefix = "Line " + (i + 1) + ": ";
            if (line == null) {
                errors.add(prefix + "payload is required");
                continue;
            }
            requireText(line.getDesignation(), prefix + "designation is required", errors);
            requirePositive(line.getQuantity(), prefix + "quantity must be greater than 0", errors);
            requireNonNegative(line.getUnitPriceExcludingVat(), prefix + "unit price excluding VAT must be >= 0", errors);
            requireNonNegative(line.getVatRatePercent(), prefix + "VAT rate must be >= 0", errors);
        }
    }

    private void requireText(String value, String message, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(message);
        }
    }

    private void requirePositive(BigDecimal value, String message, List<String> errors) {
        if (value == null || value.signum() <= 0) {
            errors.add(message);
        }
    }

    private void requireNonNegative(BigDecimal value, String message, List<String> errors) {
        if (value == null || value.signum() < 0) {
            errors.add(message);
        }
    }
}
