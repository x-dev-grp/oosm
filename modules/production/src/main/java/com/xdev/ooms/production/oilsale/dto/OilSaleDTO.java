package com.xdev.ooms.production.oilsale.dto;


import com.xdev.ooms.production.oilsale.entity.OilSale;
import com.xdev.ooms.production.storageunit.dto.StorageUnitDto;
import com.xdev.ooms.production.supplier.dto.SupplierDto;
import com.xdev.ooms.sharedkernel.Enum.*;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for OilSale entity
 */
@Getter
@Setter
public class OilSaleDTO extends BaseDto<OilSale> {
    private String invoiceNumber;
    private SaleStatus status;
    private LocalDateTime saleDate;
    private SupplierDto supplier;
    private StorageUnitDto storageUnit;
    private Olive_Oil_Type oilType;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private Currency currency;
    private PaymentMethod paymentMethod;
    private String bankAccount;
    private String checkNumber;
    private String externalTransactionId;
    private String description;
    private LocalDateTime deliveryDate;
    private String deliveryAddress;
    private String deliveryNotes;
    private boolean paid = false;
    private QualityGrades qualityGrade;
    private Double paidAmount;
    private Double unpaidAmount;
    private List<OilContainerSaleLineDto> containerSales = new ArrayList<>();
}