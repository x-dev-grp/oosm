package com.xdev.ooms.finance.internal;

import com.xdev.ooms.finance.financialtransaction.entity.FinancialTransaction;
import com.xdev.ooms.sharedkernel.ports.InvoiceNumberPort;
import com.xdev.ooms.sharedkernel.utils.BusinessCodeGenerator;
import org.springframework.stereotype.Service;

@Service
public class InvoiceNumberPortImpl implements InvoiceNumberPort {

    private final BusinessCodeGenerator businessCodeGenerator;

    public InvoiceNumberPortImpl(BusinessCodeGenerator businessCodeGenerator) {
        this.businessCodeGenerator = businessCodeGenerator;
    }

    @Override
    public synchronized String nextInvoiceNumber() {
        return businessCodeGenerator.generate(FinancialTransaction.class, "invoiceReference", "INV");
    }
}
