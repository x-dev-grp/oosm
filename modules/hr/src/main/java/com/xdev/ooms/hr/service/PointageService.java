package com.xdev.ooms.hr.service;

import com.xdev.ooms.hr.Dtos.PointageDto;
import com.xdev.ooms.hr.model.Pointage;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class PointageService extends BaseServiceImpl<Pointage, PointageDto, PointageDto> {

    public PointageService(BaseRepository<Pointage> repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
    }



}