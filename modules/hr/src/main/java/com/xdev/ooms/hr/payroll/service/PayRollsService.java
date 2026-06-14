package com.xdev.ooms.hr.payroll.service;

import com.xdev.ooms.hr.payroll.dto.PayRollsDto;
import com.xdev.ooms.hr.payroll.entity.PayRolls;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class PayRollsService extends BaseServiceImpl<PayRolls, PayRollsDto, PayRollsDto> {

    public PayRollsService(BaseRepository<PayRolls> repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
    }



}