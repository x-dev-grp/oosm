package com.xdev.ooms.production.repository;

import com.xdev.ooms.production.model.OilContainerSale;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OilContainerSaleRepo extends BaseRepository<OilContainerSale> {

    List<OilContainerSale> findByOilSaleId(UUID oilSaleId);

    @Query("""
            select o
            from OilContainerSale o
            where o.container.id = :containerId
              and o.isDeleted = false
            """)
    List<OilContainerSale> findOilContainerSalesByContainerIdAndDeletedIsFalse(@Param("containerId") UUID containerId);

}