package com.xdev.ooms.production.repository;

import com.xdev.ooms.production.model.MillMachine;
import com.xdev.ooms.production.model.OilContainer;
import com.xdev.ooms.production.model.OilContainerSale;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OilContainerRepository extends BaseRepository<OilContainer> {
   }
