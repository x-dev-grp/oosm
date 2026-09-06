package com.xdev.ooms.production.filtration.service;

import com.xdev.ooms.production.filtration.dto.FiltrationDashDto;
import com.xdev.ooms.production.filtration.entity.FiltrationOperation;
import com.xdev.ooms.production.filtration.repository.FiltrationOperationRepo;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class FiltrationDashService
        extends BaseServiceImpl<FiltrationOperation, FiltrationDashDto, FiltrationDashDto> {

    public FiltrationDashService(
            BaseRepository<FiltrationOperation> repository,
            FiltrationOperationRepo filtrationOperationRepo,
            ModelMapper modelMapper
    ) {
        super(repository, modelMapper);
    }

    @Override
    public Set<Action> actionsMapping(FiltrationOperation entity) {
        return Set.of(Action.READ, Action.UPDATE, Action.DELETE);
    }

    @Override
    protected String getEntityType() {
        return "FILTRATIONOPERATION";
    }

    @Override
    protected String getLabel(FiltrationOperation entity) {
        if (entity == null) {
            return "Filtration";
        }
        if (entity.getSourceLotNumber() != null && !entity.getSourceLotNumber().isBlank()) {
            return entity.getSourceLotNumber();
        }
        if (entity.getTargetLotNumber() != null && !entity.getTargetLotNumber().isBlank()) {
            return entity.getTargetLotNumber();
        }
        return entity.getId() != null ? "Filtration " + entity.getId() : "Filtration";
    }

    @Override
    protected String getStatus(FiltrationOperation entity) {
        if (entity == null || entity.getStatus() == null) {
            return "UNKNOWN";
        }
        return entity.getStatus().name();
    }

    @Override
    protected String getMobileRoute() {
        return "/production/filtration";
    }

    @Override
    protected String getWebRoute(FiltrationOperation entity) {
        if (entity == null || entity.getId() == null) {
            return "/production/filtration";
        }
        return "/production/filtration/" + entity.getId();
    }
}
