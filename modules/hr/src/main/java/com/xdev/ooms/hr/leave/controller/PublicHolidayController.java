package com.xdev.ooms.hr.leave.controller;

import com.xdev.ooms.hr.leave.dto.PublicHolidayDto;
import com.xdev.ooms.hr.leave.entity.PublicHoliday;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/public-holidays")
public class PublicHolidayController extends BaseControllerImpl<PublicHoliday, PublicHolidayDto, PublicHolidayDto> {

    public PublicHolidayController(
            BaseService<PublicHoliday, PublicHolidayDto, PublicHolidayDto> baseService,
            ModelMapper modelMapper
    ) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "PUBLICHOLIDAY";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
