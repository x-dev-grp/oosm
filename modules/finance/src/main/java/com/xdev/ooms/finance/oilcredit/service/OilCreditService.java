package com.xdev.ooms.finance.oilcredit.service;


import com.xdev.ooms.finance.oilcredit.dto.OilCreditDto;
import com.xdev.ooms.finance.oilcredit.entity.OilCredit;
import com.xdev.ooms.sharedkernel.basetype.entity.BaseType;
import com.xdev.ooms.sharedkernel.basetype.service.GenericTypeService;
import com.xdev.ooms.sharedkernel.Enum.TransactionState;
import com.xdev.ooms.sharedkernel.Enum.TransactionType;
import com.xdev.ooms.sharedkernel.communicator.models.shared.OilTransactionDTO;
import com.xdev.ooms.sharedkernel.ports.OilCreditPort;
import com.xdev.ooms.sharedkernel.ports.OilTransactionPort;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
public class OilCreditService extends BaseServiceImpl<OilCredit, OilCreditDto, OilCreditDto> {
   private static final Logger log = LoggerFactory.getLogger(OilCreditService.class);

   private final OilTransactionPort oilTransactionPort;
    private final OilCreditPort oilCreditPort;
    private final GenericTypeService baseTypeService;

    public OilCreditService(
            BaseRepository<OilCredit> repository,
            ModelMapper modelMapper,
            OilTransactionPort oilTransactionPort,
            OilCreditPort oilCreditPort,
            GenericTypeService baseTypeService) {
        super(repository, modelMapper);
        this.oilTransactionPort = oilTransactionPort;
        this.oilCreditPort = oilCreditPort;
        this.baseTypeService = baseTypeService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OilCreditDto save(OilCreditDto request) {
        OilTransactionDTO oilTransaction = new OilTransactionDTO();
        oilTransaction.setTransactionState(TransactionState.PENDING);
        oilTransaction.setQuantityKg(request.getQuantity());
        oilTransaction.setTransactionType(TransactionType.LOAN);
        oilTransaction.setTotalPrice(0.0);
        oilTransaction.setUnitPrice(0.0);

        OilTransactionDTO created = oilTransactionPort.create(oilTransaction);
        if (created == null || created.getId() == null) {
            log.error("Oil transaction creation failed for oil credit request");
            throw new RuntimeException("Failed to create oil transaction");
        }

        UUID createdId = created.getId();
        log.info("Successfully created oil transaction with ID: {}", createdId);

        OilCredit oilCredit = modelMapper.map(request, OilCredit.class);
        oilCredit.setTransaction_id_out(createdId);
        BaseType baseType = baseTypeService.handelBaseType(request.getOil_type());
        oilCredit.setOil_type(baseType);
        oilCredit = repository.save(oilCredit);
        log.info("Saved oil credit with ID: {} and transaction ID: {}",
                oilCredit.getId(), createdId);

        return modelMapper.map(oilCredit, OilCreditDto.class);
    }


    public void approuveOilCredit(UUID transactionId) {
        oilCreditPort.approveOilCredit(transactionId);
    }

}
