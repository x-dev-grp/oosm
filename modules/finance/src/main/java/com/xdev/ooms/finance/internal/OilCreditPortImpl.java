package com.xdev.ooms.finance.internal;

import com.xdev.ooms.finance.oilcredit.entity.OilCredit;
import com.xdev.ooms.finance.oilcredit.repository.OilCreditRepository;
import com.xdev.ooms.sharedkernel.Enum.CreditState;
import com.xdev.ooms.sharedkernel.ports.OilCreditPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class OilCreditPortImpl implements OilCreditPort {

    private static final Logger log = LoggerFactory.getLogger(OilCreditPortImpl.class);

    private final OilCreditRepository oilCreditRepository;

    public OilCreditPortImpl(OilCreditRepository oilCreditRepository) {
        this.oilCreditRepository = oilCreditRepository;
    }

    @Override
    public void approveOilCredit(UUID transactionId) {
        OilCredit oilCredit = oilCreditRepository.findByTransactionIdOut(transactionId).orElse(null);
        if (Objects.nonNull(oilCredit)) {
            oilCredit.setCreditState(CreditState.APPROVED);
            oilCreditRepository.save(oilCredit);
            log.info("Approved oil credit for transaction ID: {}", transactionId);
        } else {
            log.warn("No oil credit found for transaction ID: {}", transactionId);
        }
    }
}
