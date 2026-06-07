package com.xdev.ooms.finance.service;


import com.xdev.ooms.finance.dto.OilCreditDto;
import  com.xdev.ooms.sharedkernel.Enum.CreditState;
import com.xdev.ooms.finance.feignClients.services.OilTransactionFeignService;
import com.xdev.ooms.finance.model.OilCredit;
import com.xdev.ooms.finance.repo.OilCreditRepository;
import com.xdev.ooms.production.model.BaseType;
import com.xdev.ooms.production.service.GenericTypeService;
 import  com.xdev.ooms.sharedkernel.Enum.TransactionState;
import  com.xdev.ooms.sharedkernel.Enum.TransactionType;
import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.apiDTOs.ApiSingleResponse;
import com.xdev.ooms.sharedkernel.communicator.models.shared.OilTransactionDTO;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


@Service
public class OilCreditService extends BaseServiceImpl<OilCredit, OilCreditDto, OilCreditDto> {
   private static final Logger log = LoggerFactory.getLogger(OilCreditService.class);
   
   private final OilCreditRepository oilCreditRepository;
    private final OilTransactionFeignService oilTransactionFeignService;
    private final GenericTypeService baseTypeService;

    public OilCreditService(BaseRepository<OilCredit> repository, ModelMapper modelMapper, OilCreditRepository oilCreditRepository, OilTransactionFeignService oilTransactionFeignService, GenericTypeService baseTypeService) {
        super(repository, modelMapper);
        this.oilCreditRepository = oilCreditRepository;
        this.oilTransactionFeignService = oilTransactionFeignService;
        this.baseTypeService = baseTypeService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OilCreditDto save(OilCreditDto request) {
        // 1) Build your transaction DTO
        OilTransactionDTO oilTransaction = new OilTransactionDTO();
        oilTransaction.setTransactionState(TransactionState.PENDING);
        oilTransaction.setQuantityKg(request.getQuantity());
        oilTransaction.setTransactionType(TransactionType.LOAN);
        oilTransaction.setTotalPrice(0.0);
        oilTransaction.setUnitPrice(0.0);

        // 2) Invoke Feign synchronously and get the response
        ApiSingleResponse<OilTransactionDTO> response;
        try {
            response = oilTransactionFeignService
                    .create(oilTransaction)
                    .get();  // blocks until the remote call completes
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error calling oilTransactionFeignService.create()", e);
            throw new RuntimeException("Failed to create oil transaction", e);
        }

        // 3) Check for success and extract the ID
        if (response == null || !response.isSuccess() || response.getData() == null) {
            log.error("Oil transaction creation failed: {}", response);
            throw new RuntimeException("Failed to create oil transaction");
        }
        UUID createdId = response.getData().getExternalId();
        log.info("Successfully created oil transaction with ID: {}", createdId);

        // 4) Only now set the transaction ID and save the credit
        OilCredit oilCredit = modelMapper.map(request, OilCredit.class);
        oilCredit.setTransaction_id_out(createdId);
        BaseType baseType=baseTypeService.handelBaseType(request.getOil_type());
        oilCredit.setOil_type(baseType);
        oilCredit = repository.save(oilCredit);
        log.info("Saved oil credit with ID: {} and transaction ID: {}",
                oilCredit.getId(), createdId);

        return modelMapper.map(oilCredit, OilCreditDto.class);
    }


    public void approuveOilCredit(UUID transactionId) {
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
