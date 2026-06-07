package com.xdev.ooms.production.service;


import com.xdev.ooms.production.dto.*;
import com.xdev.ooms.production.model.QualityControlRule;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class QualityControlRuleService extends BaseServiceImpl<QualityControlRule, QualityControlRuleDto, QualityControlRuleDto> {
    public QualityControlRuleService(BaseRepository<QualityControlRule> repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
    }

    @Override
    public Set<Action> actionsMapping(QualityControlRule rule) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));

        return actions;
    }
}