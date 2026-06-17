package com.xdev.ooms.inventory.stocksec.service;

import com.xdev.ooms.inventory.stocksec.dto.MouvementStockSecDto;
import com.xdev.ooms.inventory.stocksec.entity.MouvementStockSec;
import com.xdev.ooms.inventory.stocksec.repository.MouvementStockSecRepository;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class MouvementStockSecService
        extends BaseServiceImpl<MouvementStockSec, MouvementStockSecDto, MouvementStockSecDto> {

    public MouvementStockSecService(
            BaseRepository<MouvementStockSec> repository,
            MouvementStockSecRepository mouvementStockSecRepository,
            ModelMapper modelMapper
    ) {
        super(repository, modelMapper);
    }

    @Override
    public Set<Action> actionsMapping(MouvementStockSec entity) {
        return Set.of(Action.READ);
    }
}
