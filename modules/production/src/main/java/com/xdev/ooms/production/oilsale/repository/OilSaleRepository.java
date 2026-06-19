package com.xdev.ooms.production.oilsale.repository;



import com.xdev.ooms.production.oilsale.entity.OilSale;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OilSaleRepository extends BaseRepository<OilSale> {

    @Query("""
            SELECT DISTINCT s FROM OilSale s
            LEFT JOIN FETCH s.supplier
            WHERE s.id = :id AND s.isDeleted = false
            """)
    Optional<OilSale> findByIdForPdf(@Param("id") UUID id);
} 