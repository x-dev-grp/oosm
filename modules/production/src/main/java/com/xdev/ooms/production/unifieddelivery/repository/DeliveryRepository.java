package com.xdev.ooms.production.unifieddelivery.repository;


import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.sharedkernel.Enum.DeliveryType;
import com.xdev.ooms.sharedkernel.Enum.OliveLotStatus;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Every query is scoped to the current tenant. The unscoped method names are kept as default
 * methods that resolve the tenant from {@link TenantContext} and refuse to run without one.
 */
public interface DeliveryRepository extends BaseRepository<UnifiedDelivery> {

    private static UUID requireTenant() {
        UUID tenant = TenantContext.getCurrentTenant();
        if (tenant == null) {
            throw new AccessDeniedException("Tenant context is required");
        }
        return tenant;
    }

    /** The delivery with this id in the current tenant. */
    default Optional<UnifiedDelivery> findOwned(UUID id) {
        return id == null ? Optional.empty() : findByIdAndTenantIdAndIsDeletedFalse(id, requireTenant());
    }

    @Query("""
            SELECT DISTINCT d FROM UnifiedDelivery d
            LEFT JOIN FETCH d.supplier
            LEFT JOIN FETCH d.parcel
            LEFT JOIN FETCH d.oliveVariety
            LEFT JOIN FETCH d.region
            LEFT JOIN FETCH d.storageUnit
            LEFT JOIN FETCH d.qualityControlResults qc
            LEFT JOIN FETCH qc.rule
            WHERE d.id = :id AND d.tenantId = :tenantId AND d.isDeleted = false
            """)
    Optional<UnifiedDelivery> findByIdForPdf(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    default Optional<UnifiedDelivery> findByIdForPdf(UUID id) {
        return findByIdForPdf(id, requireTenant());
    }

    @Query("""
            SELECT d
              FROM UnifiedDelivery d
             WHERE d.tenantId = :tenantId
               AND d.isDeleted = false
               AND d.lotOliveNumber = (
                       SELECT d2.lotNumber
                         FROM UnifiedDelivery d2
                        WHERE d2.id = :deliveryId
                          AND d2.tenantId = :tenantId
                   )
            """)
    List<UnifiedDelivery> findAllByLotOliveNumber(@Param("deliveryId") UUID deliveryId, @Param("tenantId") UUID tenantId);

    default UnifiedDelivery findByLotOliveNumber(UUID deliveryId) {
        return findAllByLotOliveNumber(deliveryId, requireTenant()).stream().findFirst().orElse(null);
    }

    List<UnifiedDelivery> findByTenantIdAndLotNumberInAndIsDeletedFalse(UUID tenantId, Set<String> lotNumbers);

    default List<UnifiedDelivery> findByLotNumberIn(Set<String> lotNumbers) {
        return findByTenantIdAndLotNumberInAndIsDeletedFalse(requireTenant(), lotNumbers);
    }

    List<UnifiedDelivery> findAllByTenantIdAndLotNumberAndDeliveryTypeAndIsDeletedFalse(UUID tenantId, String lotNumber, DeliveryType deliveryType);

    default List<UnifiedDelivery> findAllByLotNumberAndDeliveryTypeAndIsDeletedFalse(String lotNumber, DeliveryType deliveryType) {
        return findAllByTenantIdAndLotNumberAndDeliveryTypeAndIsDeletedFalse(requireTenant(), lotNumber, deliveryType);
    }

    /** First non-deleted delivery of this lot and type; null when there is none. */
    default UnifiedDelivery findByLotNumberAndDeliveryType(String lotNumber, DeliveryType deliveryType) {
        return findAllByLotNumberAndDeliveryTypeAndIsDeletedFalse(lotNumber, deliveryType).stream().findFirst().orElse(null);
    }

    List<UnifiedDelivery> findAllByTenantIdAndStorageUnitIdAndDeliveryTypeAndIsDeletedFalse(UUID tenantId, UUID storageUnitId, DeliveryType deliveryType);

    default List<UnifiedDelivery> findAllByStorageUnitIdAndDeliveryTypeAndIsDeletedFalse(UUID storageUnitId, DeliveryType deliveryType) {
        return findAllByTenantIdAndStorageUnitIdAndDeliveryTypeAndIsDeletedFalse(requireTenant(), storageUnitId, deliveryType);
    }

    List<UnifiedDelivery> findByTenantIdAndGlobalLotNumberAndIsDeletedFalse(UUID tenantId, String globalLotNumber);

    default List<UnifiedDelivery> findByGlobalLotNumber(String globalLotNumber) {
        return findByTenantIdAndGlobalLotNumberAndIsDeletedFalse(requireTenant(), globalLotNumber);
    }

    List<UnifiedDelivery> findByTenantIdAndGlobalLotNumberAndDeliveryTypeAndIsDeletedFalse(UUID tenantId, String globalLotNumber, DeliveryType deliveryType);

    default List<UnifiedDelivery> findByGlobalLotNumberAndDeliveryTypeAndIsDeletedFalse(String globalLotNumber, DeliveryType deliveryType) {
        return findByTenantIdAndGlobalLotNumberAndDeliveryTypeAndIsDeletedFalse(requireTenant(), globalLotNumber, deliveryType);
    }

    /**
     * Olive deliveries that can be placed on the milling board.
     * The common predicates (tenant, deliveryType, isDeleted) apply to all OR branches.
     */
    @Query("""
            SELECT d
              FROM UnifiedDelivery d
             WHERE d.tenantId = :tenantId
               AND d.deliveryType = 'OLIVE'
               AND d.isDeleted = false
               AND (
                     (d.operationType = 'BASE'             AND d.status IN ('OLIVE_CONTROLLED','IN_PROGRESS','PROD_READY'))
                  OR (d.operationType = 'OLIVE_PURCHASE'  AND d.status IN ('IN_PROGRESS','PROD_READY'))
                  OR (d.operationType = 'PAYMENT'         AND d.status IN ('OLIVE_CONTROLLED','IN_PROGRESS','PROD_READY'))
                  OR (d.operationType = 'EXCHANGE'        AND d.status IN ('OLIVE_CONTROLLED','IN_PROGRESS','PROD_READY')
                          AND COALESCE(d.unitPrice, 0) <> 0)
                  OR (d.operationType = 'SIMPLE_RECEPTION'AND d.status IN ('OLIVE_CONTROLLED','IN_PROGRESS','PROD_READY'))
               )
            """)
    List<UnifiedDelivery> findOliveDeliveriesControlled(@Param("tenantId") UUID tenantId);

    default List<UnifiedDelivery> findOliveDeliveriesControlled() {
        return findOliveDeliveriesControlled(requireTenant());
    }

    // If qualityControlResults is a collection, IS EMPTY is correct (not IS NULL).
    @Query("""
            SELECT u
              FROM UnifiedDelivery u
             WHERE u.tenantId = :tenantId
               AND u.deliveryType IN :types
               AND u.qualityControlResults IS EMPTY
               AND u.isDeleted = false
            """)
    List<UnifiedDelivery> findByDeliveryTypeInAndQualityControlResultsIsNull(@Param("types") List<String> types, @Param("tenantId") UUID tenantId);

    default List<UnifiedDelivery> findByDeliveryTypeInAndQualityControlResultsIsNull(List<String> types) {
        return findByDeliveryTypeInAndQualityControlResultsIsNull(types, requireTenant());
    }

    @Query("""
            SELECT d
              FROM UnifiedDelivery d
             WHERE d.tenantId = :tenantId
               AND d.supplier.id = :supplierId
               AND d.isDeleted = false
            """)
    List<UnifiedDelivery> findBySupplierId(@Param("supplierId") UUID supplierId, @Param("tenantId") UUID tenantId);

    default List<UnifiedDelivery> findBySupplierId(UUID supplierId) {
        return findBySupplierId(supplierId, requireTenant());
    }

    // Fully paid = both price and paidAmount are non-null, and paidAmount ≥ price
    @Query("""
            SELECT d
              FROM UnifiedDelivery d
             WHERE d.tenantId       = :tenantId
               AND d.supplier.id    = :supplierId
               AND d.price          IS NOT NULL
               AND d.paidAmount     IS NOT NULL
               AND d.paidAmount    >= d.price
               AND d.deliveryType   = 'OLIVE'
               AND d.operationType  = 'SIMPLE_RECEPTION'
               AND d.isDeleted      = false
            """)
    List<UnifiedDelivery> findFullyPaidDeliveriesBySupplierId(@Param("supplierId") UUID supplierId, @Param("tenantId") UUID tenantId);

    default List<UnifiedDelivery> findFullyPaidDeliveriesBySupplierId(UUID supplierId) {
        return findFullyPaidDeliveriesBySupplierId(supplierId, requireTenant());
    }

    // Not fully paid = either no payment or payment < price
    @Query("""
            SELECT d
              FROM UnifiedDelivery d
             WHERE d.tenantId = :tenantId
               AND d.supplier.id = :supplierId
               AND (
                     d.paidAmount IS NULL
                  OR d.price      IS NULL
                  OR d.paidAmount <  d.price
               )
               AND d.deliveryType = 'OLIVE'
               AND d.isDeleted    = false
            """)
    List<UnifiedDelivery> findUnpaidDeliveriesBySupplierId(@Param("supplierId") UUID supplierId, @Param("tenantId") UUID tenantId);

    default List<UnifiedDelivery> findUnpaidDeliveriesBySupplierId(UUID supplierId) {
        return findUnpaidDeliveriesBySupplierId(supplierId, requireTenant());
    }

    // Count fully paid (note: JPQL must use property name isDeleted, not column is_deleted)
    @Query("""
            SELECT COUNT(d)
              FROM UnifiedDelivery d
             WHERE d.tenantId      = :tenantId
               AND d.supplier.id   = :supplierId
               AND d.price         IS NOT NULL
               AND d.paidAmount    IS NOT NULL
               AND d.paidAmount   >= d.price
               AND d.deliveryType  = 'OLIVE'
               AND d.isDeleted     = false
            """)
    long countFullyPaidDeliveriesBySupplierId(@Param("supplierId") UUID supplierId, @Param("tenantId") UUID tenantId);

    default long countFullyPaidDeliveriesBySupplierId(UUID supplierId) {
        return countFullyPaidDeliveriesBySupplierId(supplierId, requireTenant());
    }

    @Query("""
            SELECT COUNT(d)
              FROM UnifiedDelivery d
             WHERE d.tenantId = :tenantId
               AND d.supplier.id = :supplierId
               AND (
                     d.paidAmount IS NULL
                  OR d.price      IS NULL
                  OR d.paidAmount <  d.price
               )
               AND d.deliveryType = 'OLIVE'
               AND d.isDeleted    = false
            """)
    long countUnpaidDeliveriesBySupplierId(@Param("supplierId") UUID supplierId, @Param("tenantId") UUID tenantId);

    default long countUnpaidDeliveriesBySupplierId(UUID supplierId) {
        return countUnpaidDeliveriesBySupplierId(supplierId, requireTenant());
    }

    @Query("""
            SELECT d
              FROM UnifiedDelivery d
             WHERE d.tenantId = :tenantId
               AND d.millMachine.id = :mill
               AND d.status = :status
               AND d.isDeleted = false
            """)
    List<UnifiedDelivery> findByMillMachineIdAndStatus(@Param("mill") UUID mill, @Param("status") OliveLotStatus status, @Param("tenantId") UUID tenantId);

    default List<UnifiedDelivery> findByMillMachineIdAndStatus(UUID mill, OliveLotStatus status) {
        return findByMillMachineIdAndStatus(mill, status, requireTenant());
    }

    /**
     * Highest delivery number used by the tenant in the given year, deleted receptions included so a
     * number is never handed out twice.
     */
    @Query(value = """
            SELECT COALESCE(MAX(CAST(d.delivery_number AS INTEGER)), 0)
              FROM unified_delivery d
             WHERE d.tenant_id = :tenantId
               AND d.delivery_number ~ '^[0-9]{1,9}$'
               AND EXTRACT(YEAR FROM COALESCE(d.delivery_date, d.created_date)) = :year
            """, nativeQuery = true)
    int findMaxDeliveryNumber(@Param("tenantId") UUID tenantId, @Param("year") int year);

    Optional<UnifiedDelivery> findFirstByDescriptionContainingIgnoreCaseAndIsDeletedFalse(String descriptionFragment);

    Optional<UnifiedDelivery> findFirstByTenantIdAndDescriptionContainingIgnoreCaseAndIsDeletedFalse(java.util.UUID tenantId, String descriptionFragment);
}
