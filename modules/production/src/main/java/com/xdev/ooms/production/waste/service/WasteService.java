package com.xdev.ooms.production.waste.service;

import com.xdev.ooms.production.unifieddelivery.dto.PaymentDTO;
import com.xdev.ooms.production.waste.dto.WasteDTO;
import com.xdev.ooms.sharedkernel.ports.FinancialTransactionPort;
import com.xdev.ooms.sharedkernel.ports.InvoiceNumberPort;
import com.xdev.ooms.production.waste.entity.Waste;
import com.xdev.ooms.production.waste.repository.WasteRepository;
import com.xdev.ooms.sharedkernel.Enum.*;
import com.xdev.ooms.sharedkernel.communicator.models.shared.FinancialTransactionDto;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

    @Override
    @Transactional
    public WasteDTO delete(UUID id) {
        if (id == null) {
            return null;
        }
        Waste waste = wasteRepository.findByIdAndIsDeletedFalse(id).orElse(null);
        if (waste == null) {
            return null;
        }
        financialTransactionPort.reverseLinked(waste.getId().toString(), ResourceName.Waste);
        waste.setPaidAmount(0.0);
        if (waste.getTotalPrice() != null) {
            waste.setUnpaidAmount(waste.getTotalPrice().doubleValue());
        }
        waste.setPaid(false);
        waste.setDeleted(true);
        return modelMapper.map(wasteRepository.save(waste), WasteDTO.class);
    }

    @Transactional
    public void processPayment(PaymentDTO paymentDTO) {
        if (paymentDTO.getIdOperation() == null) {
            throw new IllegalArgumentException("Payment operation id is required");
        }
        Waste waste = wasteRepository.findByIdAndIsDeletedFalse(paymentDTO.getIdOperation()).orElse(null);
        if (waste == null) {
            throw new IllegalArgumentException("Waste Sale not found for ID: " + paymentDTO.getIdOperation());
        }
        BigDecimal paidAmount = waste.getPaidAmount() != null ? BigDecimal.valueOf(waste.getPaidAmount()) : BigDecimal.ZERO;
        BigDecimal unpaidAmount = waste.getUnpaidAmount() != null ? BigDecimal.valueOf(waste.getUnpaidAmount()) : BigDecimal.ZERO;

        double payment = paymentDTO.getAmount() != null ? paymentDTO.getAmount() : 0d;
        if (payment <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than 0");
        }
        if (unpaidAmount.signum() <= 0) {
            throw new IllegalArgumentException("Waste sale is already fully paid: " + waste.getId());
        }
        if (payment > unpaidAmount.doubleValue()) {
            payment = unpaidAmount.doubleValue();
        }

        waste.setPaid(Math.abs(payment - unpaidAmount.doubleValue()) < 0.001);
        waste.setPaidAmount((paidAmount.add(BigDecimal.valueOf(payment))).doubleValue());
        waste.setUnpaidAmount((unpaidAmount.subtract(BigDecimal.valueOf(payment))).doubleValue());

        wasteRepository.save(waste);
        prepareFinanacalTransaction(paymentDTO, payment, waste, TransactionDirection.INBOUND, TransactionType.WASTE_SALE, OperationType.WASTE_SALE);
    }

    private void prepareFinanacalTransaction(PaymentDTO paymentDTO, double amount, Waste waste, TransactionDirection direction, TransactionType transactionType, OperationType wasteSale) {
        FinancialTransactionDto financialTransactionDto = new FinancialTransactionDto();
        financialTransactionDto.setTransactionType(transactionType);
        financialTransactionDto.setDirection(direction);
        financialTransactionDto.setAmount(BigDecimal.valueOf(amount));
        financialTransactionDto.setCurrency(paymentDTO.getCurrency() != null ? paymentDTO.getCurrency() : Currency.TND);
        financialTransactionDto.setPaymentMethod(paymentDTO.getPaymentMethod() != null ? paymentDTO.getPaymentMethod() : PaymentMethod.CASH);
        financialTransactionDto.setBankAccount(paymentDTO.getBankAccount() != null ? paymentDTO.getBankAccount() : null);
        financialTransactionDto.setCheckNumber(paymentDTO.getCheckNumber() != null ? paymentDTO.getCheckNumber() : null);
        financialTransactionDto.setLotNumber(null);
        financialTransactionDto.setsupplier(paymentDTO.getSupplier() != null
                ? modelMapper.map(paymentDTO.getSupplier(), com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto.class)
                : null);
        financialTransactionDto.setTransactionDate(LocalDateTime.now());
        financialTransactionDto.setApproved(true);
        financialTransactionDto.setApprovalDate(LocalDateTime.now());
        financialTransactionDto.setExternalTransactionId(waste.getId().toString());
        financialTransactionDto.setResourceName(ResourceName.Waste);
        financialTransactionDto.setOperationType(wasteSale);
        if (waste.getInvoiceNumber() != null && !waste.getInvoiceNumber().isBlank()) {
            financialTransactionDto.setInvoiceReference(waste.getInvoiceNumber());
        }
        financialTransactionDto.setDescription(
                "Paiement vente déchets ("
                        + (waste.getInvoiceNumber() != null ? waste.getInvoiceNumber() : waste.getId())
                        + ")");
        financialTransactionDto.setSyncProductionState(false);

        financialTransactionPort.record(financialTransactionDto);
    }

    @Override
    protected String getEntityType() {
        return "WASTE";
    }

    @Override
    protected String getLabel(Waste entity) {
        if (entity == null) {
            return "Waste";
        }
        if (entity.getInvoiceNumber() != null && !entity.getInvoiceNumber().isBlank()) {
            return entity.getInvoiceNumber();
        }
        return entity.getId() != null ? "Waste " + entity.getId() : "Waste";
    }

    @Override
    protected String getStatus(Waste entity) {
        if (entity == null) {
            return "UNKNOWN";
        }
        return Boolean.TRUE.equals(entity.getPaid()) ? "PAID" : "UNPAID";
    }

    @Override
    protected String getMobileRoute() {
        return "/production/waste";
    }

    @Override
    protected String getWebRoute(Waste entity) {
        if (entity == null || entity.getId() == null) {
            return "/production/waste";
        }
        return "/production/waste/" + entity.getId();
    }
}
