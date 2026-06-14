package com.xdev.ooms.production.oilcontainer.repository;



import com.xdev.ooms.production.millmachine.entity.MillMachine;
import com.xdev.ooms.production.oilcontainer.entity.OilContainer;
import com.xdev.ooms.production.oilcontainersale.entity.OilContainerSale;
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
