package com.xdev.ooms.production.oiltransaction.repository;



import  com.xdev.ooms.sharedkernel.Enum.TransactionType;
import com.xdev.ooms.production.oiltransaction.entity.OilTransaction;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OilTransactionRepository extends BaseRepository<OilTransaction> {

    @Query("""
            SELECT DISTINCT t FROM OilTransaction t
            LEFT JOIN FETCH t.storageUnitSource
            LEFT JOIN FETCH t.storageUnitDestination
            LEFT JOIN FETCH t.reception r
            LEFT JOIN FETCH r.supplier
            LEFT JOIN FETCH r.region
            WHERE t.id = :id AND t.isDeleted = false
            """)
    Optional<OilTransaction> findByIdForPdf(@Param("id") UUID id);
    List<OilTransaction> findByStorageUnitDestinationId(UUID storageUnitId);
    // Add this method for payment validation
    List<OilTransaction> findByOilSaleId(UUID oilSaleId);
    Optional<OilTransaction> findByOilSaleIdAndIsDeletedFalse(UUID oilSaleId);
    Optional<OilTransaction> findFirstByOilSaleIdOrderByCreatedDateDesc(UUID oilSaleId);
    Optional<OilTransaction> findFirstByStorageUnitDestinationIdAndTransactionTypeOrderByCreatedDateAsc(UUID storageUnitId, TransactionType transactionType);

    List<OilTransaction> findAllByStorageUnitDestinationIdAndTransactionTypeAndIsDeletedFalseOrderByCreatedDateAsc(
            UUID storageUnitId, TransactionType transactionType);
}
