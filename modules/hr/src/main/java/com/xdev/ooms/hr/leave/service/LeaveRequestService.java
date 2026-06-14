package com.xdev.ooms.hr.leave.service;

import com.xdev.ooms.hr.leave.dto.LeaveRequestDto;
import com.xdev.ooms.hr.leave.entity.LeaveRequest;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class LeaveRequestService extends BaseServiceImpl<LeaveRequest, LeaveRequestDto, LeaveRequestDto> {

    public LeaveRequestService(BaseRepository<LeaveRequest> repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
    }



}