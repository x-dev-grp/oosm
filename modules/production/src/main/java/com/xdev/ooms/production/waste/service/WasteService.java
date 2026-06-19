package com.xdev.ooms.production.waste.service;

import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;

import com.xdev.ooms.production.supplier.entity.Supplier;
import com.xdev.ooms.production.unifieddelivery.dto.PaymentDTO;
import com.xdev.ooms.production.waste.dto.WasteDTO;



import com.xdev.ooms.sharedkernel.ports.FinancialTransactionPort;
import com.xdev.ooms.sharedkernel.ports.InvoiceNumberPort;
import com.xdev.ooms.production.waste.entity.Waste;
import com.xdev.ooms.production.waste.repository.WasteRepository;
import  com.xdev.ooms.sharedkernel.Enum.*;
import com.xdev.ooms.sharedkernel.communicator.models.shared.FinancialTransactionDto;
import com.xdev.ooms.production.supplier.dto.SupplierDto;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class WasteService extends BaseServiceImpl<Waste, WasteDTO, WasteDTO> {

    private final FinancialTransactionPort financialTransactionPort;
    private final WasteRepository wasteRepository;
    private final InvoiceNumberPort invoiceNumberPort;

    public WasteService(WasteRepository repository,
                        ModelMapper modelMapper,
                        FinancialTransactionPort financialTransactionPort,
                        WasteRepository wasteRepository,
                        InvoiceNumberPort invoiceNumberPort) {
        super(repository, modelMapper);
        this.financialTransactionPort = financialTransactionPort;
        this.wasteRepository = wasteRepository;
        this.invoiceNumberPort = invoiceNumberPort;
    }

    @Override
    @Transactional
    public WasteDTO save(WasteDTO request) {
        if (request.getInvoiceNumber() == null || request.getInvoiceNumber().isBlank()) {
            request.setInvoiceNumber(invoiceNumberPort.nextInvoiceNumber());
        }
        return super.save(request);
    }

    @Override
    public Set<Action> actionsMapping(Waste waste) {
        Set<Action> actions = new HashSet<>();
        actions.add(Action.UPDATE);
        actions.add(Action.DELETE);
        actions.add(Action.READ);
        return actions;
    }

    @Transactional
    public void processPayment(PaymentDTO paymentDTO) {
        if (paymentDTO.getIdOperation() == null) {
            return;
        }
        Waste waste = wasteRepository.findByIdAndIsDeletedFalse(paymentDTO.getIdOperation()).orElse(null);
        if (waste == null) {
            throw new IllegalArgumentException("Waste Sale not found for ID: " + paymentDTO.getIdOperation());
        }
        BigDecimal paidAmount = waste.getPaidAmount() != null ? BigDecimal.valueOf(waste.getPaidAmount()) : BigDecimal.ZERO;
        BigDecimal unpaidAmount = waste.getUnpaidAmount() != null ? BigDecimal.valueOf(waste.getUnpaidAmount()) : BigDecimal.ZERO;

        double payment = paymentDTO.getAmount() != null ? paymentDTO.getAmount() : 0d;

        waste.setPaid(payment > 0 && payment == unpaidAmount.doubleValue());
        waste.setPaidAmount((paidAmount.add(BigDecimal.valueOf(payment))).doubleValue());
        waste.setUnpaidAmount((unpaidAmount.subtract(BigDecimal.valueOf(payment))).doubleValue());

        wasteRepository.save(waste);
        prepareFinanacalTransaction(paymentDTO, payment, waste, TransactionDirection.INBOUND, TransactionType.WASTE_SALE, OperationType.WASTE_SALE);

    }
    private void prepareFinanacalTransaction(PaymentDTO paymentDTO, double amount, Waste delivery, TransactionDirection direction, TransactionType transactionType, OperationType wasteSale) {
        // Build Financial Transaction DTO
        FinancialTransactionDto financialTransactionDto = new FinancialTransactionDto();
        financialTransactionDto.setTransactionType(transactionType);
        financialTransactionDto.setDirection(direction);
        financialTransactionDto.setAmount(BigDecimal.valueOf(amount));
        financialTransactionDto.setCurrency(paymentDTO.getCurrency() != null ? paymentDTO.getCurrency() : Currency.TND);
        financialTransactionDto.setPaymentMethod(paymentDTO.getPaymentMethod() != null ? paymentDTO.getPaymentMethod() : PaymentMethod.CASH);
        financialTransactionDto.setBankAccount(paymentDTO.getBankAccount() != null ? paymentDTO.getBankAccount() : null);
        financialTransactionDto.setCheckNumber(paymentDTO.getCheckNumber() != null ? paymentDTO.getCheckNumber() : null);
        financialTransactionDto.setLotNumber("N/A");
        financialTransactionDto.setsupplier(paymentDTO.getSupplier() != null ? modelMapper.map(paymentDTO.getSupplier(), com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto.class): null);
        financialTransactionDto.setTransactionDate(LocalDateTime.now());
        financialTransactionDto.setApproved(true);
        financialTransactionDto.setApprovalDate(LocalDateTime.now());
        financialTransactionDto.setApprovedBy(null);
        financialTransactionDto.setExternalTransactionId(delivery.getExternalId().toString());
        financialTransactionDto.setOperationType(wasteSale);
        if (delivery.getInvoiceNumber() != null && !delivery.getInvoiceNumber().isBlank()) {
            financialTransactionDto.setInvoiceReference(delivery.getInvoiceNumber());
        }

        // Send to finance service
        financialTransactionPort.record(financialTransactionDto);
    }
}
