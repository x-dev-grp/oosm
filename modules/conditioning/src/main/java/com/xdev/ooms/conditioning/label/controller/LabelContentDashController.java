package com.xdev.ooms.conditioning.label.controller;

import com.xdev.ooms.conditioning.label.dto.LabelContentDashDto;
import com.xdev.ooms.conditioning.label.entity.LabelContent;
import com.xdev.ooms.conditioning.label.service.LabelContentDashService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ordreConditionement/label-contents")
public class LabelContentDashController
        extends BaseControllerImpl<LabelContent, LabelContentDashDto, LabelContentDashDto> {

    public LabelContentDashController(LabelContentDashService service, ModelMapper modelMapper) {
        super(service, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "LABELCONTENT";
    }
}
