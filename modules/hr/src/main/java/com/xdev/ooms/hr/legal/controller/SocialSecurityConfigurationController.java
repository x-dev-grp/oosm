package com.xdev.ooms.hr.legal.controller;

import com.xdev.ooms.hr.legal.dto.SocialSecurityConfigurationDto;
import com.xdev.ooms.hr.legal.entity.SocialSecurityConfiguration;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/social-security-configs")
public class SocialSecurityConfigurationController extends BaseControllerImpl<SocialSecurityConfiguration, SocialSecurityConfigurationDto, SocialSecurityConfigurationDto> {

    public SocialSecurityConfigurationController(
            BaseService<SocialSecurityConfiguration, SocialSecurityConfigurationDto, SocialSecurityConfigurationDto> baseService,
            ModelMapper modelMapper
    ) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "SOCIALSECURITYCONFIG";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
